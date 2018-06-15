package com.shotgun.viewserver.error;

import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "clientErrorController")
public class ClientErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ClientErrorController.class);

    @ControllerAction(path = "log", isSynchronous = true)
    public void log(@ActionParam(name = "error") String error,
                      @ActionParam(name = "isFatal") Boolean isFatal){

        String userId = (String) ControllerContext.get("userId");
        String formattedError = String.format("[CLIENT_ERROR] userId: %s isFatal: %s - %s", userId != null ? userId : "", isFatal, error);

        logger.error(formattedError);

    }
}
