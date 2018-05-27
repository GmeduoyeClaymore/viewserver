package io.viewserver.server.components;

import io.viewserver.authentication.*;
import io.viewserver.command.*;
import io.viewserver.sql.ExecuteSqlCommandHandler;
import rx.Observable;

public class BasicSubscriptionComponent implements IBasicSubscriptionComponent{


    private final AuthenticationHandlerRegistry authenticationHandlerRegistry = new AuthenticationHandlerRegistry();
    private IBasicServerComponents basicServerComponents;


    public BasicSubscriptionComponent(IBasicServerComponents basicServerComponents) {
        this.basicServerComponents = basicServerComponents;

    }

    public Observable start() {

        // commands we can receive
        register("authenticate", new AuthenticateCommandHandler(authenticationHandlerRegistry));
        register("unsubscribe", new UnsubscribeHandler(basicServerComponents.getSubscriptionManager()));
        register("configurate", new ConfigurateCommandHandler());
        register("subscribe", new SubscribeHandler(basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(),basicServerComponents.getExecutionPlanRunner()));
        register("updateSubscription", new UpdateSubscriptionHandler(basicServerComponents.getExecutionPlanRunner()));
        register("tableEdit", new TableEditCommandHandler(basicServerComponents.getTableFactoryRegistry()));
        register("executeSql", new ExecuteSqlCommandHandler(basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(), basicServerComponents.getExecutionPlanRunner(), basicServerComponents.getExecutionContext().getSummaryRegistry()));
        this.registerAuthenticationHandlers();
        return Observable.just(true);
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
