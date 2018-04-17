package com.shotgun.viewserver.user;

import io.viewserver.controller.ControllerAction;

import java.util.HashMap;

public interface INexmoController {
    @ControllerAction(path = "getInternationalFormatNumber", isSynchronous = false)
    String getInternationalFormatNumber(String phoneNumber);
}
