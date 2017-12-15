package com.shotgun.viewserver;

import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;

/**
 * Created by Gbemiga on 13/12/17.
 */
@Controller(name = "loginController")
public class LoginController {

    @ControllerAction(path = "login", isSynchronous = true)
    public String login(@ActionParam(name = "foo")LoginCredentials credentials){
        return null;
    }
}
