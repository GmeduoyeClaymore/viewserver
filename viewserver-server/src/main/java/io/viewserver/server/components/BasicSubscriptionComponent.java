package io.viewserver.server.components;

import io.viewserver.authentication.*;
import io.viewserver.command.*;
import io.viewserver.sql.ExecuteSqlCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class BasicSubscriptionComponent implements IBasicSubscriptionComponent{


    protected final AuthenticationHandlerRegistry authenticationHandlerRegistry = new AuthenticationHandlerRegistry();
    private IBasicServerComponents basicServerComponents;
    private Logger  logger = LoggerFactory.getLogger(BasicSubscriptionComponent.class);


    public BasicSubscriptionComponent(IBasicServerComponents basicServerComponents) {
        this.basicServerComponents = basicServerComponents;

    }

    public Observable start() {
        logger.info("Starting basic subscription component");
        // commands we can receive
        register("authenticate", new AuthenticateCommandHandler(authenticationHandlerRegistry));
        register("unsubscribe", new UnsubscribeHandler(basicServerComponents.getSubscriptionManager()));
        register("configurate", new ConfigurateCommandHandler());
        register("subscribe", new SubscribeHandler(basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(),basicServerComponents.getExecutionPlanRunner()));
        register("updateSubscription", new UpdateSubscriptionHandler(basicServerComponents.getExecutionPlanRunner()));
        register("tableEdit", new TableEditCommandHandler(basicServerComponents.getTableFactoryRegistry()));
        register("executeSql", new ExecuteSqlCommandHandler(basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(), basicServerComponents.getExecutionPlanRunner(), basicServerComponents.getExecutionContext().getSummaryRegistry()));
        this.registerAuthenticationHandlers();
        logger.info("Finished basic subscription component");
        return Observable.just(true);
    }

    void register(String name, ICommandHandler commandHandler){
        basicServerComponents.getCommandHandlerRegistry().register(name,commandHandler);
    }

    protected void registerAuthenticationHandlers() {
        authenticationHandlerRegistry.register("open", new OpenAuthenticationHandler());
        authenticationHandlerRegistry.register(LoggerAuthenticationHandler.TYPE, new LoggerAuthenticationHandler());
        authenticationHandlerRegistry.register("slave", new SlaveAuthenticationHandler());
    }

}
