package com.shotgun.viewserver.error;

import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "clientLoggerController")
public class ClientLoggerController {

    private static final Logger logger = LoggerFactory.getLogger(ClientLoggerController.class);

    @ControllerAction(path = "logError", isSynchronous = false)
    public void logError(@ActionParam(name = "error") String error,
                    @ActionParam(name = "isFatal") Boolean isFatal){

        String userId = (String) ControllerContext.get("userName");
        String formattedError = String.format("[CLIENT_ERROR] userId: %s isFatal: %s - %s", userId != null ? userId : "", isFatal, error);

        logger.error(formattedError);

    }

    @ControllerAction(path = "log", isSynchronous = false)
    public void log(@ActionParam(name = "message") String message, @ActionParam(name = "level") String level){

        String userId = (String) ControllerContext.get("userName");
        String formattedError = String.format("[CLIENT_LOG] userId: %s level: %s - %s", userId != null ? userId : "", level, message);

        if("Info".equals(level)){
            logger.info(formattedError);
        }
        else if("Error".equals(level)){
            logger.info(formattedError);
        }
        else if("Debug".equals(level)){
            logger.debug(formattedError);
        }
        else {
            logger.warn(formattedError);
        }
    }
}
