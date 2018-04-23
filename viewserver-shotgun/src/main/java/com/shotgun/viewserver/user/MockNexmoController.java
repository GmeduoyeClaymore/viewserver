package com.shotgun.viewserver.user;

import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;

@Controller(name = "nexmoController")
public class MockNexmoController implements INexmoController {
    public MockNexmoController() {
    }
    @Override
    @ControllerAction(path = "getInternationalFormatNumber", isSynchronous = false)
    public String getInternationalFormatNumber(String phoneNumber) {
        return "44" + phoneNumber.substring(1);
    }

}
