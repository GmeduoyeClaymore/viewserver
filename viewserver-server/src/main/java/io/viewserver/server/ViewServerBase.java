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

import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.ConnectionManager;
import io.viewserver.command.CommandHandlerRegistry;
import io.viewserver.command.ConfigurateCommandHandler;
import io.viewserver.command.SubscriptionManager;
import io.viewserver.command.UnsubscribeHandler;
import io.viewserver.configurator.Configurator;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.messages.protobuf.Decoder;
import io.viewserver.messages.protobuf.Encoder;
import io.viewserver.messages.protobuf.Message;
import io.viewserver.network.Network;
import io.viewserver.network.SimpleNetworkMessageWheel;
import io.viewserver.network.netty.NettyNetworkAdapter;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.reactor.EventLoopReactor;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.MultiThreadedEventLoopReactor;
import io.viewserver.reactor.ITask;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by nickc on 25/11/2014.
 */
public abstract class ViewServerBase<TDataSource extends IDataSource> {
    private static final Logger log = LoggerFactory.getLogger(ViewServerBase.class);
    private final IExecutionContext serverExecutionContext = new ExecutionContext();
    private final Catalog serverCatalog = new Catalog(serverExecutionContext);
    protected final IJsonSerialiser jsonSerialiser = new JacksonSerialiser();
    private final String name;
    protected final CommandHandlerRegistry commandHandlerRegistry;
    protected ExecutionPlanRunner executionPlanRunner;
    protected OperatorFactoryRegistry operatorFactoryRegistry;
    protected Configurator configurator;
    protected IDistributionManager distributionManager;
    private Network serverNetwork;
    private IReactor serverReactor;
    protected IDataSourceRegistry<TDataSource> dataSourceRegistry;
    private final SubscriptionManager subscriptionManager = new SubscriptionManager();

    protected ViewServerBase(String name) {
        this.name = name;

        operatorFactoryRegistry = getServerExecutionContext().getOperatorFactoryRegistry();
        configurator = new Configurator(operatorFactoryRegistry);
        commandHandlerRegistry = new CommandHandlerRegistry();
    }

    public IExecutionContext getServerExecutionContext() {
        return serverExecutionContext;
    }

    public Catalog getServerCatalog() {
        return serverCatalog;
    }

    public String getName() {
        return name;
    }

    public Network getServerNetwork() {
        return serverNetwork;
    }

    public IReactor getServerReactor() {
        return serverReactor;
    }

    public void run() {
        try {
            final NettyNetworkAdapter networkAdapter = new NettyNetworkAdapter();
            final SimpleNetworkMessageWheel networkMessageWheel = new SimpleNetworkMessageWheel(new Encoder(), new Decoder());
            networkAdapter.setNetworkMessageWheel(networkMessageWheel);
            serverNetwork = new Network(commandHandlerRegistry, serverExecutionContext, serverCatalog, networkAdapter);
            serverReactor = this.initReactor(serverNetwork);

            serverReactor.start();

            serverReactor.scheduleTask(new ITask() {
                @Override
                public void execute() {
                    Runtime runtime = Runtime.getRuntime();
                    log.trace("Memory used: {}; Free memory: {}; Max memory: {}", runtime.totalMemory() - runtime.freeMemory(),
                            runtime.freeMemory(), runtime.maxMemory());
                }
            }, 1, 3 * 60 * 1000);

            CountDownLatch initLatch = new CountDownLatch(1);
            serverReactor.scheduleTask(() -> {
                initialise();
                initLatch.countDown();
            }, 0, -1);
            initLatch.await();
        } catch (Throwable e) {
            log.error("Fatal error happened during startup", e);
        }
    }

    protected void initialise() {
        // TODO: replace this with a proper way to initialise the message pool
        new Message();

        createSystemOperators();

        initCommandHandlerRegistry();
    }

    protected void createSystemOperators() {
        new ConnectionManager(serverExecutionContext, getServerCatalog(), new ChunkedColumnStorage(1024));
    }

    protected void initCommandHandlerRegistry() {
        commandHandlerRegistry.register("unsubscribe", new UnsubscribeHandler(getSubscriptionManager()));
        commandHandlerRegistry.register("configurate", new ConfigurateCommandHandler());
    }

    private IReactor initReactor(Network serverNetwork) {
        IReactor serverReactor = getReactor(serverNetwork);
        serverExecutionContext.setReactor(serverReactor);
        return serverReactor;
    }

    protected abstract IReactor getReactor(Network serverNetwork);

    public IDataSourceRegistry<TDataSource> getDataSourceRegistry() {
        return dataSourceRegistry;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }
}
