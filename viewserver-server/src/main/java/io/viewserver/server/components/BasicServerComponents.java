package io.viewserver.server.components;

import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.ConnectionManager;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandHandlerRegistry;
import io.viewserver.command.SubscriptionManager;
import io.viewserver.configurator.Configurator;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.core.JacksonSerialiser;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.messages.protobuf.Message;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.operators.table.TableFactoryRegistry;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import rx.Observable;

/**
 * Created by bemm on 25/11/2014.
 */



public class BasicServerComponents implements IBasicServerComponents {
    protected ExecutionContext serverExecutionContext;
    protected Catalog serverCatalog;
    protected OperatorFactoryRegistry operatorFactoryRegistry;
    protected Configurator configurator;
    protected CommandHandlerRegistry commandHandlerRegistry;
    protected IJsonSerialiser jsonSerialiser = new JacksonSerialiser();
    protected ExecutionPlanRunner executionPlanRunner;
    protected ConnectionManager connectionManager;
    protected SubscriptionManager subscriptionManager;
    protected TableFactoryRegistry tableFactoryRegistry;


    public BasicServerComponents() {
        new Message();//to init the message pool
        serverExecutionContext = new ExecutionContext();
        serverCatalog = new Catalog(serverExecutionContext);
        operatorFactoryRegistry = serverExecutionContext.getOperatorFactoryRegistry();
        configurator = new Configurator(operatorFactoryRegistry);
        commandHandlerRegistry = new CommandHandlerRegistry();
        executionPlanRunner = new ExecutionPlanRunner(configurator);
        subscriptionManager = new SubscriptionManager();
        connectionManager = new ConnectionManager(serverExecutionContext, getServerCatalog(), new ChunkedColumnStorage(1024));
        tableFactoryRegistry = new TableFactoryRegistry();
    }

    public BasicServerComponents(ExecutionContext serverExecutionContext, Catalog serverCatalog, OperatorFactoryRegistry operatorFactoryRegistry, Configurator configurator, CommandHandlerRegistry commandHandlerRegistry, ExecutionPlanRunner executionPlanRunner, ConnectionManager connectionManager, SubscriptionManager subscriptionManager, TableFactoryRegistry tableFactoryRegistry) {
        new Message();//to init the message pool
        this.serverExecutionContext = serverExecutionContext;
        this.serverCatalog = serverCatalog;
        this.operatorFactoryRegistry = operatorFactoryRegistry;
        this.configurator = configurator;
        this.commandHandlerRegistry = commandHandlerRegistry;
        this.executionPlanRunner = executionPlanRunner;
        this.connectionManager = connectionManager;
        this.subscriptionManager = subscriptionManager;
        this.tableFactoryRegistry = tableFactoryRegistry;
    }

    @Override
    public TableFactoryRegistry getTableFactoryRegistry() {
        return tableFactoryRegistry;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    public IExecutionPlanRunner getExecutionPlanRunner() {
        return executionPlanRunner;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public void listen() {
    }

    @Override
    public IExecutionContext getExecutionContext() {
        return serverExecutionContext;
    }

    @Override
    public ICatalog getServerCatalog() {
        return serverCatalog;
    }

    @Override
    public OperatorFactoryRegistry getOperatorFactoryRegistry() {
        return operatorFactoryRegistry;
    }

    @Override
    public Configurator getConfigurator() {
        return configurator;
    }

    @Override
    public CommandHandlerRegistry getCommandHandlerRegistry() {
        return commandHandlerRegistry;
    }

    @Override
    public IJsonSerialiser getJsonSerialiser() {
        return jsonSerialiser;
    }

    @Override
    public Observable<Object> start() {
        new Catalog("graphNodes", getServerCatalog());
        return Observable.just(true);
    }

    @Override
    public void stop() {
    }
}
