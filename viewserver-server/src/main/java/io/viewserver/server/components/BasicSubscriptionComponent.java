package io.viewserver.server.components;

import io.viewserver.authentication.*;
import io.viewserver.command.*;
import io.viewserver.controller.ControllerCatalog;
import io.viewserver.controller.ControllerJSONCommandHandler;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.report.ReportRegistry;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.sql.ExecuteSqlCommandHandler;

public class BasicSubscriptionComponent implements IBasicSubscriptionComponent{


    private final AuthenticationHandlerRegistry authenticationHandlerRegistry = new AuthenticationHandlerRegistry();
    private IBasicServerComponents basicServerComponents;


    public BasicSubscriptionComponent(IBasicServerComponents basicServerComponents) {
        this.basicServerComponents = basicServerComponents;

    }

    public void start() {

        // commands we can receive
        register("authenticate", new AuthenticateCommandHandler(authenticationHandlerRegistry));
        register("unsubscribe", new UnsubscribeHandler(basicServerComponents.getSubscriptionManager()));
        register("configurate", new ConfigurateCommandHandler());
        register("subscribe", new SubscribeHandler(basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(),basicServerComponents.getExecutionPlanRunner()));
        register("updateSubscription", new UpdateSubscriptionHandler(basicServerComponents.getExecutionPlanRunner()));
        register("tableEdit", new TableEditCommandHandler(basicServerComponents.getTableFactoryRegistry()));
        register("executeSql", new ExecuteSqlCommandHandler(basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(), basicServerComponents.getExecutionPlanRunner(), basicServerComponents.getExecutionContext().getSummaryRegistry()));
        this.registerAuthenticationHandlers();
    }

    void register(String name, ICommandHandler commandHandler){
        basicServerComponents.getCommandHandlerRegistry().register(name,commandHandler);
    }

    private void registerAuthenticationHandlers() {
        authenticationHandlerRegistry.register("open", new OpenAuthenticationHandler());
        authenticationHandlerRegistry.register(LoggerAuthenticationHandler.TYPE, new LoggerAuthenticationHandler());
        authenticationHandlerRegistry.register("slave", new SlaveAuthenticationHandler());
    }

}
