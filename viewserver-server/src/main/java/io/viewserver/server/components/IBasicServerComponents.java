package io.viewserver.server;

import io.viewserver.catalog.ConnectionManager;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandHandlerRegistry;
import io.viewserver.command.SubscriptionManager;
import io.viewserver.configurator.Configurator;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.operators.table.TableFactoryRegistry;

public interface IBasicServerComponents extends  IServerComponent{

    TableFactoryRegistry getTableFactoryRegistry();

    SubscriptionManager getSubscriptionManager();

    IExecutionContext getExecutionContext();

    IExecutionPlanRunner getExecutionPlanRunner();

    ICatalog getServerCatalog();

    OperatorFactoryRegistry getOperatorFactoryRegistry();

    Configurator getConfigurator();

    CommandHandlerRegistry getCommandHandlerRegistry();

    IJsonSerialiser getJsonSerialiser();

    ConnectionManager getConnectionManager();
}
