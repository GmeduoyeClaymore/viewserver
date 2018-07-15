package io.viewserver.server.components;

import io.viewserver.controller.ControllerCatalog;
import io.viewserver.controller.ControllerJSONCommandHandler;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class ControllerComponents implements IControllerComponents {

    private final ControllerJSONCommandHandler controllerHandler;
    protected IBasicServerComponents basicServerComponents;
    private ControllerCatalog controllerCatalog;
    Logger logger = LoggerFactory.getLogger(ControllerComponents.class);

    public ControllerComponents(IBasicServerComponents basicServerComponents) {
        this.controllerCatalog = new ControllerCatalog(new ChunkedColumnStorage(1024), basicServerComponents.getExecutionContext(),basicServerComponents.getServerCatalog());
        this.controllerHandler = new ControllerJSONCommandHandler(controllerCatalog);
        this.basicServerComponents = basicServerComponents;
    }

    @Override
    public Object registerController(Object controller){
        this.controllerCatalog.registerController(controller);
        return controller;
    }

    @Override
    public ControllerCatalog getControllerCatalog() {
        return controllerCatalog;
    }

    @Override
    public Observable start() {
        logger.info("Starting controller components");
        basicServerComponents.getCommandHandlerRegistry().register("genericJSON", this.controllerHandler);
        logger.info("Started controller components");

        return Observable.just(true);
    }
}
