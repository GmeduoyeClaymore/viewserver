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
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.sql.IExecuteSqlCommand;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by nick on 10/02/2015.
 */
public class ViewServerClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ViewServerClient.class);
    private static final ISubscriptionFactory<ClientSubscription> defaultSubscriptionFactory = ClientSubscription::new;
    private final ExecutionContext executionContext = new ExecutionContext();
    private final Catalog catalog = new Catalog(executionContext);
    private final String name;
    private IEndpoint endpoint;
    private Network network;
    private EventLoopReactor reactor;
    private ListenableFuture<IPeerSession> connectFuture;
    private SettableFuture<IPeerSession> authenticateFuture = SettableFuture.create();
    private CommandHandlerRegistry commandHandlerRegistry;

    public ViewServerClient(String name, IEndpoint endpoint) {
        this.name = name;
        this.endpoint = endpoint;

        run();
    }

    public ViewServerClient(String name, String url) throws URISyntaxException {
        this(name, EndpointFactoryRegistry.createEndpoint(url));
    }

    public ListenableFuture<IPeerSession> getConnectFuture() {
        return connectFuture;
    }

    public String getReactorThreadName() {
        return reactor.getOwnerThreadName();
    }

    public CommandHandlerRegistry getCommandHandlerRegistry() {
        return commandHandlerRegistry;
    }

    private void run() {
        try {
            commandHandlerRegistry = new CommandHandlerRegistry();
            registerCommands();
            final NettyNetworkAdapter networkAdapter = new NettyNetworkAdapter();
            final SimpleNetworkMessageWheel networkMessageWheel = new SimpleNetworkMessageWheel(new Encoder(), new Decoder());
            networkAdapter.setNetworkMessageWheel(networkMessageWheel);
            network = new Network(commandHandlerRegistry, executionContext, catalog, networkAdapter);
            reactor = this.initReactor(network);

            reactor.start();
            reactor.scheduleTask(() -> {
                // TODO: replace this with a proper way to initialise the message pool
                new Message();
            }, 0, -1);

            connectFuture = network.connect(endpoint);
        } catch (Throwable e) {
            log.error("Fatal error happened during startup", e);
        }
    }

    private void registerCommands() {
    }

    protected ListenableFuture<Boolean> sendCommand(Command command) {
        return sendCommand(command, true);
    }

    protected ListenableFuture<Boolean> sendCommand(Command command, boolean requireAuthentication) {
        SettableFuture<Boolean> future = SettableFuture.create();
        if (command.getMessage() != null) {
            command.getMessage().retain();
        }
        reactor.addCallback(requireAuthentication ? authenticateFuture : connectFuture, new FutureCallback<IPeerSession>() {
            @Override
            public void onSuccess(IPeerSession peerSession) {
                ICommandResultListener originalListener = command.getCommandResultListener();
                command.setCommandResultListener((result) -> {
                    if (result.isSuccess()) {
                        future.set(true);
                    } else {
                        future.setException(new ViewServerClientException(result.getMessage()));
                    }
                    if (originalListener != null) {
                        originalListener.onResult(result);
                    }
                });
                peerSession.sendCommand(command);
                command.getMessage().release();
            }

            @Override
            public void onFailure(Throwable t) {
                future.setException(t);
            }
        });
        return future;
    }

    protected EventLoopReactor initReactor(Network serverNetwork) {
        EventLoopReactor serverReactor = new EventLoopReactor(name, serverNetwork);
        executionContext.setReactor(serverReactor);
        return serverReactor;
    }

    public ListenableFuture<Boolean> authenticate(String type, String... tokens) {
        IAuthenticateCommand authenticateCommandDto = MessagePool.getInstance().get(IAuthenticateCommand.class)
                .setType(type);
        authenticateCommandDto.getTokens().addAll(Arrays.asList(tokens));

        ListenableFuture<Boolean> authenticationFuture = sendCommand(AuthenticationHandlerRegistry.AUTHENTICATE_COMMAND, authenticateCommandDto, false);
        Futures.addCallback(authenticationFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                try {
                    authenticateFuture.set(connectFuture.get());
                } catch (Exception e) {
                    // should never happen, since we won't send the auth command before the connection is established
                }
            }

            @Override
            public void onFailure(Throwable t) {
                authenticateFuture.setException(t);
            }
        });
        authenticateCommandDto.release();
        return authenticationFuture;
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
        reactor.addCallback(authenticateFuture, new FutureCallback<IPeerSession>() {
            @Override
            public void onSuccess(IPeerSession peerSession) {
                reactor.scheduleTask(() -> {
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
                }, 0, -1);
            }

            @Override
            public void onFailure(Throwable t) {
                future.setException(t);
            }
        });
        return future;
    }

    ListenableFuture<Boolean> updateSubscription(ClientSubscription subscription, Options options) {
        IUpdateSubscriptionCommand updateSubscriptionCommandDto = MessagePool.getInstance().get(IUpdateSubscriptionCommand.class)
                .setCommandId(subscription.getCommandId())
                .setOptions(options.toMessage());
        final ListenableFuture<Boolean> future = sendCommand("updateSubscription", updateSubscriptionCommandDto);
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

    public ListenableFuture<Boolean> createTable(String tableName, List<Column> columns) {
        return createTable(tableName, columns, null, null);
    }

    public ListenableFuture<Boolean> createTable(String tableName, List<Column> columns, String tableType, ITableCreationConfig config) {
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
        final ListenableFuture<Boolean> future = sendCommand("tableEdit", tableEditCommandBuilder);
        tableEditCommandBuilder.release();
        return future;
    }

    public ListenableFuture<Boolean> editTable(String tableName, Iterable<RowEvent> rowEvents, boolean reset) {
        ITableEditCommand tableEditCommand = MessagePool.getInstance().get(ITableEditCommand.class)
                .setTableName(tableName);

        ITableEvent tableEvent = tableEditCommand.getTableEvent()
                .setId(-1)
                .setInitSize(-1)
                .setExecutionCycle(-1);
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

        final ListenableFuture<Boolean> future = sendCommand("tableEdit", tableEditCommand);
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

    public ListenableFuture<Boolean> sendCommand(String commandName, IPoolableMessage commandDto) {
        return sendCommand(commandName, commandDto, true);
    }

    private ListenableFuture<Boolean> sendCommand(String commandName, IPoolableMessage commandDto, boolean requireAuthentication) {
        Command command = new Command(commandName, commandDto);
        return sendCommand(command, requireAuthentication);
    }

    @Override
    public void close() throws IOException {
        reactor.shutDown();
        reactor.waitForShutdown();

        network.reset();
    }
}
