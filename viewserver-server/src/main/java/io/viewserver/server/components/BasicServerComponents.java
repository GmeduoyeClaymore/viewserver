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

/**
 * Created by nickc on 25/11/2014.
 */



public class BasicServerComponents implements IBasicServerComponents {
    private final ExecutionContext serverExecutionContext;
    private final Catalog serverCatalog;
    private final OperatorFactoryRegistry operatorFactoryRegistry;
    private final Configurator configurator;
    private final CommandHandlerRegistry commandHandlerRegistry;
    protected final IJsonSerialiser jsonSerialiser = new JacksonSerialiser();
    private final ExecutionPlanRunner executionPlanRunner;
    private final ConnectionManager connectionManager;
    private final SubscriptionManager subscriptionManager;
    private final TableFactoryRegistry tableFactoryRegistry;


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
        new Catalog("graphNodes", getServerCatalog());

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
    public void start() {
    }
}
