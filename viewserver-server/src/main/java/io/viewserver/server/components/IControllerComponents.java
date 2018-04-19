package io.viewserver.server.components;

import io.viewserver.controller.ControllerCatalog;

public interface IControllerComponents extends IServerComponent{
    Object registerController(Object controller);
    ControllerCatalog getControllerCatalog();
}
