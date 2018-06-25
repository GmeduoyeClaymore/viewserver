/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.client;

import io.viewserver.Constants;
import io.viewserver.authentication.AuthenticationHandlerRegistry;
import io.viewserver.catalog.Catalog;
import io.viewserver.command.CommandHandlerRegistry;
import io.viewserver.command.ICommandResultListener;
import io.viewserver.core.ExecutionContext;
import io.viewserver.execution.Options;
import io.viewserver.execution.ReportContext;
import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.*;
import io.viewserver.messages.protobuf.Decoder;
import io.viewserver.messages.protobuf.Encoder;
import io.viewserver.messages.protobuf.Message;
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.messages.tableevent.ISchemaChange;
import io.viewserver.messages.tableevent.IStatus;
import io.viewserver.messages.tableevent.ITableEvent;
import io.viewserver.network.*;
import io.viewserver.network.netty.NettyNetworkAdapter;
import io.viewserver.operators.deserialiser.DeserialiserOperator;
import io.viewserver.operators.deserialiser.IDeserialiserEventHandler;
import io.viewserver.reactor.EventLoopReactor;
import io.viewserver.reactor.IReactor;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.sql.IExecuteSqlCommand;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.observable.ListenableFutureObservable;
import rx.subjects.ReplaySubject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ViewServerClient implements AutoCloseable {
    private final Logger log;
    private static final ISubscriptionFactory<ClientSubscription> defaultSubscriptionFactory = ClientSubscription::new;
    private ExecutionContext executionContext = new ExecutionContext();
    private Catalog catalog;
    private final String name;
    private ReconnectionSettings reconnectionSettings;
    private List<IEndpoint> endpoints;
    private Network network;
    private IReactor reactor;
    private ReplaySubject<IPeerSession> connectReplaySubject;
    private SettableFuture<IPeerSession> authenticateFuture = SettableFuture.create();
    private CommandHandlerRegistry commandHandlerRegistry;
    private int endpointCounter = 0;
    private Subscription authenticationSubscription;
    private List<Subscription> subscriptons;
    private String type;
    private String clientVersion;
    public boolean isClosed;

    public ViewServerClient(String name, List<IEndpoint> endpoints, ReconnectionSettings reconnectionSettings) {
        this.name = name;
        this.log = LoggerFactory.getLogger(String.format("%s-%s",ViewServerClient.class,name));
        this.reconnectionSettings = reconnectionSettings;
        this.executionContext = new ExecutionContext();
        this.catalog = new Catalog(executionContext);
        this.endpoints = endpoints;
        this.connectReplaySubject = ReplaySubject.create(1);
        this.subscriptons = new ArrayList<>();
        run();
    }

    public String getName() {
        return name;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public Network getNetwork() {
        return network;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public ViewServerClient(String name, String url,ReconnectionSettings reconnectionSettings) throws URISyntaxException {
        this(name, EndpointFactoryRegistry.createEndpoints(url),reconnectionSettings);
    }

    public Observable<IPeerSession> getConnectObservable() {
        return connectReplaySubject;
    }


    public CommandHandlerRegistry getCommandHandlerRegistry() {
        return commandHandlerRegistry;
    }

    private void run() {
        try {
            new Message();//force pool to init
            commandHandlerRegistry = new CommandHandlerRegistry();
            registerCommands();
            final NettyNetworkAdapter networkAdapter = new NettyNetworkAdapter();
            final SimpleNetworkMessageWheel networkMessageWheel = new SimpleNetworkMessageWheel(new Encoder(), new Decoder());
            networkAdapter.setNetworkMessageWheel(networkMessageWheel);
            network = new Network(commandHandlerRegistry, executionContext, catalog, networkAdapter);
            network.setDisconnectOnTimeout(true);
            network.setTimeoutInterval(4000);
            reactor = this.initReactor(network);

            reactor.start();
            reactor.scheduleTask(() -> {
                // TODO: replace this with a proper way to initialise the message pool
                new Message();
            }, 0, -1);

            this.subscriptons.add(network.connectObservable(endpoints,reconnectionSettings).subscribe(this::onConnectionEstablished, this::onConnectionError));
            this.subscriptons.add(network.disconnectionObservable().subscribe(c-> onSessionDisconnect()));
        } catch (Throwable e) {
            log.error("Fatal error happened during startup", e);
        }
    }

    private void onConnectionError(Throwable throwable) {
        if(this.isClosed){
            return;
        }
        log.info("Connection error {}");
        this.connectReplaySubject.onError(throwable);
    }

    public Observable<CommandResult> withAuthentication(String type, String clientVersion){
        this.type = type;
        this.clientVersion = clientVersion;

        if(this.authenticationSubscription != null){
            this.authenticationSubscription.unsubscribe();
        }
        log.info("MILESTONE - Attempting authentication");
        return Observable.create(subscriber ->  {
            ViewServerClient.this.authenticationSubscription = ViewServerClient.this.connectReplaySubject.subscribe(session -> {
                log.info("MILESTONE - Detected connection now authenticating");
                ViewServerClient.this.authenticate(type, clientVersion).subscribe(
                        res -> {
                            log.info("MILESTONE - Authetication succeeded - " + res);
                            subscriber.onNext(res);
                            subscriber.onCompleted();
                        },
                        err -> {
                            log.info("MILESTONE - Authetication failed - " + err);
                            subscriber.onError(err);
                        }
                );
            });
        },Emitter.BackpressureMode.BUFFER);

    }

    private void onSessionDisconnect() {
        this.connectReplaySubject = ReplaySubject.create(1);
        if(this.isClosed){
            return;
        }
        if(this.clientVersion != null){
            withAuthentication(this.type,this.clientVersion).subscribe();
        }
    }

    private void onConnectionEstablished(IPeerSession iPeerSession) {
        if(this.isClosed){
            return;
        }
        log.info("Connection established {}",iPeerSession);
        this.connectReplaySubject.onNext(iPeerSession);
    }


    private void registerCommands() {
    }


    public ListenableFuture<CommandResult> sendCommand(Command command) {
        SettableFuture<CommandResult> future = SettableFuture.create();
        if (command.getMessage() != null) {
            command.getMessage().retain();
        }

        getConnectObservable().take(1).subscribe(
                iPeerSession -> {
                    ICommandResultListener originalListener = command.getCommandResultListener();
                    command.setCommandResultListener((result) -> {
                        if (result.isSuccess()) {
                            future.set(new CommandResult(true, result.getMessage()));
                        } else {
                            future.setException(new ViewServerClientException(result.getMessage()));
                        }
                        if (originalListener != null) {
                            originalListener.onResult(result);
                        }
                    });
                    iPeerSession.sendCommand(command);
                    command.getMessage().release();
                },
                err -> {
                    future.setException(err);
                }
        );
        return future;
    }

    protected IReactor initReactor(Network serverNetwork) {
        IReactor serverReactor = new EventLoopReactor(name, serverNetwork);
        executionContext.setReactor(serverReactor);
        return serverReactor;
    }

    public Observable<CommandResult> authenticate(String type, String clientVersion) {
          IAuthenticateCommand authenticateCommandDto = MessagePool.getInstance().get(IAuthenticateCommand.class)
                .setType(type);
          log.info("Sending authentication command {} with clientVersion {}",type, clientVersion);
        authenticateCommandDto.setClientVersion(clientVersion);

        ListenableFuture<CommandResult> authenticationFuture = sendCommand(AuthenticationHandlerRegistry.AUTHENTICATE_COMMAND, authenticateCommandDto);
        return ListenableFutureObservable.from(authenticationFuture, executionContext.getReactor().getExecutor()).timeout(5,TimeUnit.SECONDS, Observable.error(new RuntimeException("No response to authenticate command in 5 seconds")));
    }

    public ListenableFuture<ClientSubscription> subscribe(String operator, Options options, ISubscriptionEventHandler<ClientSubscription> eventHandler) {
        return subscribe(operator, Constants.OUT, options, eventHandler);
    }

    public ListenableFuture<ClientSubscription> subscribe(String operator, String output, Options options, ISubscriptionEventHandler<ClientSubscription> eventHandler) {
        return subscribe(operator, output, options, eventHandler, defaultSubscriptionFactory);
    }

    public <TSubscription> ListenableFuture<TSubscription> subscribe(String operator, String output, Options options,
                                                           ISubscriptionEventHandler<TSubscription> eventHandler,
                                                           ISubscriptionFactory<TSubscription> subscriptionFactory) {
        ISubscribeCommand subscribeCommand = MessagePool.getInstance().get(ISubscribeCommand.class)
                .setOperatorName(operator)
                .setOutputName(output);
        if (options != null) {
            subscribeCommand.setOptions(options.toMessage());
        }
        Command command = new Command("subscribe", subscribeCommand);
        final ListenableFuture<TSubscription> future = sendSubscribe(command, options, eventHandler, subscriptionFactory);
        subscribeCommand.release();
        return future;
    }

    public ListenableFuture<ClientSubscription> subscribeToReport(ReportContext reportContext, Options options, ISubscriptionEventHandler<ClientSubscription> eventHandler) {
        return subscribeToReport(reportContext, options, eventHandler, defaultSubscriptionFactory);
    }

    public <TSubscription> ListenableFuture<TSubscription> subscribeToReport(ReportContext reportContext, Options options,
                                                        ISubscriptionEventHandler<TSubscription> eventHandler,
                                                        ISubscriptionFactory<TSubscription> subscriptionFactory) {
        ISubscribeReportCommand subscribeCommand = MessagePool.getInstance().get(ISubscribeReportCommand.class)
                .setReportContext(reportContext.toMessage());
        if (options != null) {
            subscribeCommand.setOptions(options.toMessage());
        }
        Command command = new Command("subscribeReport", subscribeCommand);

        final ListenableFuture<TSubscription> future = sendSubscribe(command, options, eventHandler, subscriptionFactory);
        subscribeCommand.release();
        return future;
    }

    public ListenableFuture<ClientSubscription> subscribeToDimension(String dimension, ReportContext reportContext, Options options, ISubscriptionEventHandler eventHandler) {
        return subscribeToDimension(dimension, reportContext, options, eventHandler, defaultSubscriptionFactory);
    }

    public <TSubscription> ListenableFuture<TSubscription> subscribeToDimension(String dimension, ReportContext reportContext, Options options,
                                                           ISubscriptionEventHandler<TSubscription> eventHandler,
                                                           ISubscriptionFactory<TSubscription> subscriptionFactory) {
        ISubscribeDimensionCommand subscribeCommand = MessagePool.getInstance().get(ISubscribeDimensionCommand.class)
                .setDimension(dimension)
                .setReportContext(reportContext.toMessage());
        if (options != null) {
            subscribeCommand.setOptions(options.toMessage());
        }
        Command command = new Command("subscribeDimension", subscribeCommand);

        final ListenableFuture<TSubscription> future = sendSubscribe(command, options, eventHandler, subscriptionFactory);
        subscribeCommand.release();
        return future;
    }

    private <TSubscription> ListenableFuture<TSubscription> sendSubscribe(Command command, Options options,
                                                                final ISubscriptionEventHandler<TSubscription> eventHandler,
                                                                ISubscriptionFactory<TSubscription> subscriptionFactory) {
        SettableFuture<TSubscription> future = SettableFuture.<TSubscription>create();
        command.getMessage().retain();
        this.getConnectObservable().subscribe(
                peerSession -> {
                    final DeserialiserOperator deserialiserOperator = new DeserialiserOperator(
                            UUID.randomUUID().toString(),
                            executionContext,
                            catalog,
                            peerSession,
                            "table",
                            new ChunkedColumnStorage(1024));
                    deserialiserOperator.setSubscribeCommand(command);
                    command.getMessage().release();
                    TSubscription subscription = subscriptionFactory.create(deserialiserOperator, options, ViewServerClient.this);
                    if (eventHandler != null) {
                        deserialiserOperator.addEventHandler(new IDeserialiserEventHandler() {
                            @Override
                            public void onDataReset(DeserialiserOperator deserialiserOperator) {
                                eventHandler.onDataReset(subscription);
                            }

                            @Override
                            public void onSubscriptionError(DeserialiserOperator deserialiserOperator, String msg) {
                                future.setException(new ViewServerClientException(msg));
                                eventHandler.onSubscriptionError(msg);
                            }

                            @Override
                            public void onSubscriptionSuccess(DeserialiserOperator deserialiserOperator) {
                                future.set(subscription);
                                eventHandler.onSubscriptionSuccess(subscription);
                            }

                            @Override
                            public void onSnapshotComplete(DeserialiserOperator deserialiserOperator) {
                                eventHandler.onSnapshotComplete(subscription);
                            }

                            @Override
                            public void onBatchComplete(DeserialiserOperator deserialiserOperator, int rowEventsCount) {
                                eventHandler.onBatchComplete(subscription, rowEventsCount);
                            }

                            @Override
                            public void onRowAdd(DeserialiserOperator deserialiserOperator, int rowId) {
                                eventHandler.onRowAdd(subscription, rowId, getRowValues(deserialiserOperator, rowId, null));
                            }

                            @Override
                            public void onRowUpdate(DeserialiserOperator deserialiserOperator, int rowId, IRowFlags rowFlags) {
                                eventHandler.onRowUpdate(subscription, rowId, getRowValues(deserialiserOperator, rowId, rowFlags));
                            }

                            @Override
                            public void onRowRemove(DeserialiserOperator deserialiserOperator, int rowId) {
                                eventHandler.onRowRemove(subscription, rowId);
                            }
                        });
                    }
                    deserialiserOperator.connect();
                },
                err->{
                    future.setException(err);
                }
        );
        return future;
    }

    ListenableFuture<CommandResult> updateSubscription(ClientSubscription subscription, Options options) {
        IUpdateSubscriptionCommand updateSubscriptionCommandDto = MessagePool.getInstance().get(IUpdateSubscriptionCommand.class)
                .setCommandId(subscription.getCommandId())
                .setOptions(options.toMessage());
        final ListenableFuture<CommandResult> future = sendCommand("updateSubscription", updateSubscriptionCommandDto);
        updateSubscriptionCommandDto.release();
        return future;
    }

    private Map<String, Object> getRowValues(DeserialiserOperator deserialiserOperator, int rowId, IRowFlags rowFlags) {
        HashMap<String, Object> values = new HashMap<>();

        Schema schema = deserialiserOperator.getOutput().getSchema();
        List<ColumnHolder> columnHolders = schema.getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder == null) {
                continue;
            }
            if (rowFlags != null && !rowFlags.isDirty(columnHolder.getColumnId())) {
                continue;
            }
            values.put(columnHolder.getName(), ColumnHolderUtils.getValue(columnHolder, rowId));
        }

        return values;
    }

    public ListenableFuture<CommandResult> createTable(String tableName, List<Column> columns) {
        return createTable(tableName, columns, null, null);
    }

    public ListenableFuture<CommandResult> createTable(String tableName, List<Column> columns, String tableType, ITableCreationConfig config) {
        ITableEditCommand tableEditCommandBuilder = MessagePool.getInstance().get(ITableEditCommand.class)
                .setTableName(tableName)
                .setOperation(ITableEditCommand.Operation.Create);
        if (tableType != null) {
            ITableEditCommand.ICreationConfig creationConfigBuilder = tableEditCommandBuilder.getCreationConfig()
                    .setTableType(tableType);
            if (config != null) {
                creationConfigBuilder.setConfig(config.serialise());
            }
        }
        ITableEvent tableEvent = tableEditCommandBuilder.getTableEvent()
                .setId(-1)
                .setInitSize(-1)
                .setExecutionCycle(-1);
        ISchemaChange schemaChangeBuilder = tableEvent.getSchemaChange()
                .setSchemaSize(columns.size());
        for (Column column : columns) {
            final ISchemaChange.IAddColumn addColumn = MessagePool.getInstance().get(ISchemaChange.IAddColumn.class)
                    .setName(column.getName())
                    .setType(column.getColumnType().serialise());
            schemaChangeBuilder.getAddColumns().add(addColumn);
            addColumn.release();
        }
        final ListenableFuture<CommandResult> future = sendCommand("tableEdit", tableEditCommandBuilder);
        tableEditCommandBuilder.release();
        return future;
    }

    public ListenableFuture<CommandResult> editTable(String tableName, Iterable<RowEvent> rowEvents, List<Column> columns,ITableEditCommand.ICreationConfig creationConfig, boolean reset) {

        ITableEditCommand tableEditCommand = MessagePool.getInstance().get(ITableEditCommand.class)
                .setCreationConfig(creationConfig)
                .setTableName(tableName);

        ITableEvent tableEvent = tableEditCommand.getTableEvent()
                .setId(-1)
                .setInitSize(-1)
                .setExecutionCycle(-1);

        if(columns != null){
            ISchemaChange schemaChangeBuilder = tableEvent.getSchemaChange()
                    .setSchemaSize(columns.size());
            for (Column column : columns) {
                final ISchemaChange.IAddColumn addColumn = MessagePool.getInstance().get(ISchemaChange.IAddColumn.class)
                        .setName(column.getName())
                        .setType(column.getColumnType().serialise());
                schemaChangeBuilder.getAddColumns().add(addColumn);
                addColumn.release();
            }
        }

        if (reset) {
            final IStatus status = MessagePool.getInstance().get(IStatus.class).setStatusId(IStatus.StatusId.DataReset);
            tableEvent.getStatuses().add(status);
            status.release();
        }

        for (RowEvent rowEvent : rowEvents) {
            IRowEvent rowEventDto = MessagePool.getInstance().get(IRowEvent.class)
                    .setRowId(rowEvent.getRowId())
                    .setType(getDtoRowEventType(rowEvent.getType()));
            final List columnValues = rowEventDto.getColumnValues();

            rowEvent.getValues().entrySet().forEach(value -> {
                IRowEvent.IColumnValue columnValue = MessagePool.getInstance().get(IRowEvent.IColumnValue.class)
                        .setColumnId(value.getKey());
                if (value.getValue() == null) {
                    columnValue.setNullValue();
                } else if (value.getValue() instanceof Boolean) {
                    columnValue.setBooleanValue((boolean) value.getValue());
                } else if (value.getValue() instanceof Integer) {
                    columnValue.setIntegerValue((int) value.getValue());
                } else if (value.getValue() instanceof Long) {
                    columnValue.setLongValue((long) value.getValue());
                } else if (value.getValue() instanceof Float) {
                    columnValue.setFloatValue((float) value.getValue());
                } else if (value.getValue() instanceof Double) {
                    columnValue.setDoubleValue((double) value.getValue());
                } else if (value.getValue() instanceof String) {
                    String stringValue = (String) value.getValue();

                    if (stringValue != null) {
                        columnValue.setStringValue(stringValue);
                    } else {
                        columnValue.setNullValue();
                    }
                }
                columnValues.add(columnValue);
                columnValue.release();
            });
            tableEvent.getRowEvents().add(rowEventDto);
            rowEventDto.release();
        }

        final ListenableFuture<CommandResult> future = sendCommand("tableEdit", tableEditCommand);
        tableEditCommand.release();
        return future;
    }

    public ListenableFuture<ClientSubscription> executeSql(String query, boolean permanent,
                                                           ISubscriptionEventHandler<ClientSubscription> eventHandler) {
        IExecuteSqlCommand builder = MessagePool.getInstance().get(IExecuteSqlCommand.class)
                .setQuery(query)
                .setPermanent(permanent);
        Command command = new Command("executeSql", builder);
        final ListenableFuture<ClientSubscription> future = sendSubscribe(command, null, eventHandler, defaultSubscriptionFactory);
        builder.release();
        return future;
    }

    private IRowEvent.Type getDtoRowEventType(RowEventType eventType) {
        switch (eventType) {
            case Add: {
                return IRowEvent.Type.Add;
            }
            case Update: {
                return IRowEvent.Type.Update;
            }
            case Remove: {
                return IRowEvent.Type.Remove;
            }
            default: {
                throw new IllegalArgumentException("Unknown row event type " + eventType);
            }
        }
    }


    private ListenableFuture<CommandResult> sendCommand(String commandName, IPoolableMessage commandDto) {
        Command command = new Command(commandName, commandDto);
        return sendCommand(command);
    }

    @Override
    public void close() {
        if(this.isClosed){
            return;
        }
        this.connectReplaySubject.onCompleted();
        this.isClosed = true;
        network.shutdown();
        log.info("Closing client - " + name);
        this.subscriptons.forEach(c-> c.unsubscribe());
        reactor.shutDown();
        reactor.waitForShutdown();
    }
}
