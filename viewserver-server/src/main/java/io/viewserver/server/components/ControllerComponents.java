package io.viewserver.server.components;

import io.viewserver.controller.ControllerCatalog;
import io.viewserver.controller.ControllerJSONCommandHandler;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

public class ControllerComponents implements IControllerComponents {

    private final ControllerJSONCommandHandler controllerHandler;
    protected IBasicServerComponents basicServerComponents;
    private ControllerCatalog controllerCatalog;

    public ControllerComponents(IBasicServerComponents basicServerComponents) {
        this.controllerHandler = new ControllerJSONCommandHandler(controllerCatalog);
        this.basicServerComponents = basicServerComponents;
    }

    public Object registerController(Object controller){
        this.controllerCatalog.registerController(controller);
        return controller;
    }

    @Override
    public void start() {
        this.controllerCatalog = new ControllerCatalog(new ChunkedColumnStorage(1024), basicServerComponents.getExecutionContext(),basicServerComponents.getServerCatalog());
        basicServerComponents.getCommandHandlerRegistry().register("genericJSON", this.controllerHandler);
    }
}
