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

package io.viewserver.server;

import io.viewserver.authentication.*;
import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.*;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.datasource.*;
import io.viewserver.distribution.CoalescorFactory;
import io.viewserver.distribution.DistributionManager;
import io.viewserver.distribution.DistributionOperatorFactory;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.SystemReportExecutor;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.plan.JoinMultiContextHandler;
import io.viewserver.execution.plan.MultiContextHandlerRegistry;
import io.viewserver.execution.plan.UnionGroupMultiContextHandler;
import io.viewserver.execution.plan.UnionTransposeMultiContextHandler;
import io.viewserver.expression.function.IUserDefinedFunction;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.network.IEndpoint;
import io.viewserver.network.Network;
import io.viewserver.network.netty.inproc.NettyInProcEndpoint;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.operators.table.RollingTableFactory;
import io.viewserver.operators.table.TableFactoryRegistry;
import io.viewserver.operators.unenum.UnEnumOperatorFactory;
import io.viewserver.reactor.EventLoopReactor;
import io.viewserver.reactor.IReactor;
import io.viewserver.report.ReportContextRegistry;
import io.viewserver.report.ReportDistributor;
import io.viewserver.report.ReportRegistry;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.server.distribution.RegisterSlaveCommandHandler;
import io.viewserver.sql.ExecuteSqlCommandHandler;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewServerMaster extends ViewServerBase<DataSource> implements IDataSourceListener {
    private static final Logger log = LoggerFactory.getLogger(ViewServerMaster.class);
    private static final String DATASOURCE_DEPENDENCY_PREFIX = "/" + IDataSourceRegistry.TABLE_NAME + "/";
    protected final DimensionMapper dimensionMapper = new DimensionMapper();
    private final H2LocalStorageDataAdapterFactory localStorageDataAdapterFactory;
    private ExecutionPlanRunner executionPlanRunner;
    protected ReportRegistry reportRegistry;
    protected MultiContextHandlerRegistry multiContextHandlerRegistry;
    protected ReportContextRegistry reportContextRegistry;
    private IViewServerMasterConfiguration configuration;
    private final ListeningExecutorService dataLoaderExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(new DataLoaderThreadFactory()));
    private final TableFactoryRegistry tableFactoryRegistry = new TableFactoryRegistry();
    private final AuthenticationHandlerRegistry authenticationHandlerRegistry = new AuthenticationHandlerRegistry();
    private SystemReportExecutor systemReportExecutor;
    private ControllerJSONCommandHandler controllerHandler;

    public ViewServerMaster(String name, IViewServerMasterConfiguration configuration) {
        super(name);
        this.configuration = configuration;
        localStorageDataAdapterFactory = new H2LocalStorageDataAdapterFactory(configuration.getMasterDatabasePath());
        controllerHandler = new ControllerJSONCommandHandler();
    }

    public ReportRegistry getReportRegistry() {
        return reportRegistry;
    }

    protected void registerFunction(String name, Class<? extends IUserDefinedFunction> function) {
        getServerExecutionContext().getFunctionRegistry().register(name, function);
    }

    protected IViewServerMasterConfiguration getConfiguration() {
        return configuration;
    }

    protected ListeningExecutorService getDataLoaderExecutor() {
        return dataLoaderExecutor;
    }

    @Override
    protected void initialise() {
        dataSourceRegistry = new LocalStorageDataSourceRegistry(getServerCatalog(), getServerExecutionContext(), jsonSerialiser,
                localStorageDataAdapterFactory);
        dataSourceRegistry.addListener(this);
        executionPlanRunner = new ExecutionPlanRunner(configurator, distributionManager);
        getServerExecutionContext().getExpressionParser().setDimensionMapper(dimensionMapper);
        distributionManager = new DistributionManager(IInitialiseSlaveCommand.Type.Master,
                getDataSourceRegistry(),
                jsonSerialiser,
                getServerExecutionContext(),
                dimensionMapper,
                getServerCatalog()
        );
        executionPlanRunner.setDistributionManager(distributionManager);

        // reporting
        // TODO: move into a "module"
        new Catalog("graphNodes", getServerCatalog());
        reportRegistry = new ReportRegistry(getServerCatalog(), getServerExecutionContext(), jsonSerialiser, localStorageDataAdapterFactory, dataLoaderExecutor);
        reportContextRegistry = new ReportContextRegistry(getServerExecutionContext(), getServerCatalog(), new ChunkedColumnStorage(1024));
        multiContextHandlerRegistry = new MultiContextHandlerRegistry();
        multiContextHandlerRegistry.register("uniongroup", new UnionGroupMultiContextHandler(distributionManager, executionPlanRunner));
        multiContextHandlerRegistry.register("uniontranspose", new UnionTransposeMultiContextHandler(distributionManager, executionPlanRunner));
        multiContextHandlerRegistry.register("join", new JoinMultiContextHandler(distributionManager, executionPlanRunner, getServerExecutionContext().getExpressionParser()));
        systemReportExecutor = new SystemReportExecutor(multiContextHandlerRegistry, dimensionMapper, executionPlanRunner, distributionManager, dataSourceRegistry, reportRegistry);
        ReportDistributor reportDistributor = new ReportDistributor(getServerExecutionContext(), getServerCatalog(), reportContextRegistry, systemReportExecutor);
        distributionManager.addNodeMonitor(reportDistributor, false);
        reportContextRegistry.setDistributionManager(distributionManager);
        reportRegistry.loadReports();

        super.initialise();

        registerAuthenticationHandlers();

        distributionManager.initialise();

        registerOperatorFactories();

        registerTableFactories();

        NettyInProcEndpoint inProcEndpoint = null;
        try {
            inProcEndpoint = new NettyInProcEndpoint("inproc://master");
        } catch (URISyntaxException e) {
        }
        getServerNetwork().listen(inProcEndpoint);
        Iterable<IEndpoint> masterEndpoints = configuration.getMasterEndpoints();
        if (masterEndpoints != null) {
            for (IEndpoint endpoint : masterEndpoints) {
                getServerNetwork().listen(endpoint);
            }
        }

        this.registerDataSources();
    }

    private void registerAuthenticationHandlers() {
        authenticationHandlerRegistry.register("open", new OpenAuthenticationHandler());
        authenticationHandlerRegistry.register(LoggerAuthenticationHandler.TYPE, new LoggerAuthenticationHandler());
        authenticationHandlerRegistry.register("slave", new SlaveAuthenticationHandler());
    }

    private void registerOperatorFactories() {
        OperatorFactoryRegistry operatorFactoryRegistry = getServerExecutionContext().getOperatorFactoryRegistry();
        operatorFactoryRegistry.register(new UnEnumOperatorFactory(dimensionMapper));
        operatorFactoryRegistry.register(new CoalescorFactory(distributionManager, getServerExecutionContext().getSummaryRegistry()));
        operatorFactoryRegistry.register(new DistributionOperatorFactory(distributionManager));
    }

    private void registerTableFactories() {
        tableFactoryRegistry.register(new RollingTableFactory());
    }

    private void registerDataSources() {
        if (!Boolean.parseBoolean(System.getProperty("server.bypassDataSources", "false"))) {
            final ListenableFuture<?> future = dataLoaderExecutor.submit(() -> {
                ((LocalStorageDataSourceRegistry) getDataSourceRegistry()).loadDataSources();
            });
            getServerReactor().addCallback(future, new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("Failed to register data sources", t);
                    // TODO: what should we do now?
                }
            });
        }
    }

    public void runDataSource(final DataSource dataSource) {
        getServerReactor().scheduleTask(() -> {
            try {
                if (!checkDependencies(dataSource)) {
                    return;
                }

                // TODO: put this logic somewhere more appropriate
                final ICatalog dataSourceCatalog = ((DataSourceRegistryBase) dataSourceRegistry).getChild(dataSource.getName());
                getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.INITIALIZING);

                if(dataSource.getDataLoader() != null) {
                    ITableUpdater tableUpdater;
                    if (dataSource.hasOption(DataSourceOption.IsPartition)) {
                        tableUpdater = new TablePartitionUpdater(getServerExecutionContext(), dataSourceCatalog,
                                dataSource.getPartitionConfig());
                    } else if (dataSource.hasOption(DataSourceOption.IsKeyed)) {
                        boolean isWritable = dataSource.hasOption(DataSourceOption.IsWritable);
                        IDataAdapter dataAdapter = dataSource.getDataLoader().getDataAdapter();
                        if (isWritable && dataAdapter instanceof IWritableDataAdapter) {
                            tableUpdater = new LocalPersistentKeyedTableUpdater(getServerExecutionContext(), dataSourceCatalog,
                                    (IWritableDataAdapter) dataAdapter);
                        } else {
                            if (isWritable) {
                                log.warn("Data source {} is writable, but its data adapter is not", dataSource.getName());
                            }
                            tableUpdater = new LocalKeyedTableUpdater(getServerExecutionContext(), dataSourceCatalog);
                        }
                    } else {
                        tableUpdater = new LocalTableUpdater(getServerExecutionContext(), dataSourceCatalog);
                    }
                    dataSource.initialise(dimensionMapper,
                            tableUpdater,
                            getServerExecutionContext().getFunctionRegistry(), getServerExecutionContext().getExpressionParser(),
                            getServerExecutionContext());
                }



                CommandResult planResult = new CommandResult();
                planResult.setListener(res -> {
                    if (res.isSuccess()) {
                        dataSourceRegistry.setStatus(dataSource.getName(), DataSourceStatus.BUILT);
                        if(dataSource.getDataLoader() != null){
                            getServerReactor().addCallback(dataSource.getDataLoader().getLoadDataFuture(), new FutureCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean o) {
                                    getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.INITIALIZED);
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    log.error("Problem occurred loading data for " + dataSource.getName(), throwable);
                                    getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.ERROR);
                                }
                            });
                            dataLoaderExecutor.submit((Runnable) dataSource::loadData);
                        }else{
                            getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.INITIALIZED);
                        }
                    } else {
                        log.error("Problem occurred loading data for " + dataSource.getName(), res.getMessage());
                        getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.ERROR);
                    }
                });
                DataSourceHelper.runDataSourceExecutionPlan(dataSource, dataSourceRegistry, getServerExecutionContext(),
                        IDataSourceRegistry.getDataSourceCatalog(dataSource, getServerCatalog()),
                        distributionManager, planResult);
            } catch (Throwable t) {
                log.error("Problem occurred loading data for " + dataSource.getName(), t);
                getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.ERROR);
            }
        }, 0, -1);
    }

    private boolean checkDependencies(DataSource dependingDataSource) {
        final Set<String> dependencies = new HashSet<>();
        final List<IGraphNode> nodes = dependingDataSource.getNodes();
        final int nodeCount = nodes.size();
        for (int i = 0; i < nodeCount; i++) {
            final IGraphNode node = nodes.get(i);
            final List<IConfiguratorSpec.Connection> connections = node.getConnections();
            final int connectionCount = connections.size();
            for (int j = 0; j < connectionCount; j++) {
                final IConfiguratorSpec.Connection connection = connections.get(j);
                final String operator = connection.getOperator();
                if (operator.startsWith(DATASOURCE_DEPENDENCY_PREFIX)) {
                    final String suffix = operator.substring(DATASOURCE_DEPENDENCY_PREFIX.length());
                    final int slashIndex = suffix.indexOf("/");
                    if (slashIndex > -1) {
                        final String dependencyName = suffix.substring(0, slashIndex);
                        final DataSourceStatus status = dataSourceRegistry.getStatus(dependencyName);
                        if (status != DataSourceStatus.BUILT && status != DataSourceStatus.INITIALIZED) {
                            dependencies.add(dependencyName);
                        }
                    }
                }
            }
        }
        if (dependencies.isEmpty()) {
            log.debug("{} has no outstanding dependencies - will build", dependingDataSource.getName());
            return true;
        }

        dataSourceRegistry.addListener(new IDataSourceListener() {
            @Override
            public void onDataSourceRegistered(IDataSource dataSource) {
            }

            @Override
            public void onDataSourceStatusChanged(IDataSource dataSource, DataSourceStatus status) {
                if (dependencies.contains(dataSource.getName()) && (status == DataSourceStatus.BUILT
                        || status == DataSourceStatus.INITIALIZED)) {
                    dependencies.remove(dataSource.getName());
                    if (dependencies.isEmpty()) {
                        runDataSource(dependingDataSource);
                    }
                }
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("{} has outstanding dependencies - {} - not building now", dependingDataSource.getName(),
                    StringUtils.join(dependencies, ','));
        }
        return false;
    }

    @Override
    protected void initCommandHandlerRegistry() {
        super.initCommandHandlerRegistry();

        // commands we can receive
        commandHandlerRegistry.register("authenticate", new AuthenticateCommandHandler(authenticationHandlerRegistry));
        commandHandlerRegistry.register("subscribe", new SubscribeHandler(getSubscriptionManager(), distributionManager, configurator, executionPlanRunner));
        commandHandlerRegistry.register("updateSubscription", new UpdateSubscriptionHandler(configurator, distributionManager));
        commandHandlerRegistry.register("reset", new ResetCommandHandler(getDataSourceRegistry(), distributionManager, getServerExecutionContext(), getServerCatalog(), dimensionMapper));
        commandHandlerRegistry.register("registerSlave", new RegisterSlaveCommandHandler(distributionManager));
        commandHandlerRegistry.register("tableEdit", new TableEditCommandHandler(getDataSourceRegistry(), dimensionMapper, tableFactoryRegistry));

        commandHandlerRegistry.register("subscribeReport", new SubscribeReportHandler(dimensionMapper, getDataSourceRegistry(), reportRegistry, getSubscriptionManager(), distributionManager, configurator, executionPlanRunner, reportContextRegistry, systemReportExecutor));
        commandHandlerRegistry.register("subscribeDataSource", new SubscribeDataSourceHandler(getDataSourceRegistry(), getSubscriptionManager(), distributionManager, configurator, executionPlanRunner));
        commandHandlerRegistry.register("subscribeDimension", new SubscribeDimensionHandler(dimensionMapper, getDataSourceRegistry(), reportRegistry, getSubscriptionManager(), distributionManager, getServerExecutionContext().getOperatorFactoryRegistry(), configurator, executionPlanRunner));
        commandHandlerRegistry.register("genericJSON", this.controllerHandler);
        commandHandlerRegistry.register("executeSql", new ExecuteSqlCommandHandler(getSubscriptionManager(),
                distributionManager, configurator, executionPlanRunner, getServerExecutionContext().getSummaryRegistry()));
    }

    @Override
    protected IReactor getReactor(Network serverNetwork) {
        return new EventLoopReactor(super.getName(), serverNetwork);
    }

    public Object registerController(Object controller){
        this.controllerHandler.registerController(controller);
        return controller;
    }

    @Override
    public void onDataSourceRegistered(IDataSource dataSource) {
        this.runDataSource((DataSource) dataSource);
    }

    @Override
    public void onDataSourceStatusChanged(IDataSource dataSource, DataSourceStatus status) {
    }

    private static class DataLoaderThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DataLoaderThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "data-loader-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
