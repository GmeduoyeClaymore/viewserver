package io.viewserver.controller;

import com.google.common.util.concurrent.*;
import io.viewserver.command.CommandHandlerBase;
import io.viewserver.command.CommandResult;
import io.viewserver.messages.command.IGenericJSONCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Created by Gbemiga on 12/12/17.
 */

public class ControllerJSONCommandHandler extends CommandHandlerBase<IGenericJSONCommand> {
    private static final Logger log = LoggerFactory.getLogger(ControllerJSONCommandHandler.class);

    private ListeningExecutorService asyncExecutor = MoreExecutors.newDirectExecutorService();
    private ListeningExecutorService reactorExecutor;
    public ControllerJSONCommandHandler(ControllerCatalog controllerCatalog) {
        super(IGenericJSONCommand.class);
        this.controllerCatalog = controllerCatalog;

    }

    private ControllerCatalog controllerCatalog;

    public ControllerJSONCommandHandler(Class<IGenericJSONCommand> clazz, ControllerCatalog controllerCatalog) {
        super(clazz);
        this.controllerCatalog = controllerCatalog;
    }

    public String trim(String param){
        try {
            if (param == null) {
                return null;
            }
            if (param.length() < 4000) {
                return param;
            }
            return param.substring(0, 4000);
        }catch (Exception ex){
            return "null";
        }
    }

    @Override
    protected void handleCommand(Command command, IGenericJSONCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            String controllerName = data.getPath();
            if(controllerName == null || "".equals(controllerName)){
                throw new RuntimeException("\"path\" argument not specified on command message");
            }
            String action = data.getAction();
            if(action == null || "".equals(action)){
                throw new RuntimeException("\"action\" argument not specified on command message");
            }
            ControllerRegistration registration = controllerCatalog.getController(controllerName);
            if(registration == null){
                throw new RuntimeException("Unable to find registration for controller named \"" + controllerName + "\"");
            }
            log.info(String.format("JSON command controller:\"%s\" action:\"%s\" payload:\"%s\"",controllerName,trim(action),trim(data.getPayload())));
            ControllerActionEntry entry = registration.getActions().get(data.getAction());
            if(entry == null){
                throw new RuntimeException("Unable to find action named \"" + data.getAction() + "\" in controller named \"" + controllerName + "\" containing actions \"" + String.join(",",registration.getActions().keySet()) + "\"" );
            }
            String payload = data.getPayload();
            ListenableFuture<String> invoke = invoke(entry, payload, ControllerContext.create(peerSession));
            invoke.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message = invoke.get();
                        log.info(String.format("JSON command controller:\"%s\" action:\"%s\" result:\"%s\"",controllerName,trim(action),message));
                        commandResult.setSuccess(true).setMessage(message).setComplete(true);
                    } catch (InterruptedException e) {
                        log.error(String.format("Failed to handle JSON command :\"%s\" action:\"%s\"",controllerName,trim(action)), e);
                        commandResult.setSuccess(false).setMessage(ControllerContext.Unwrap(e).getMessage()).setComplete(true);
                    } catch (ExecutionException e) {
                        log.error(String.format("Failed to handle JSON command :\"%s\" action:\"%s\"",controllerName,trim(action)), e);
                        commandResult.setSuccess(false).setMessage(ControllerContext.Unwrap(e).getMessage()).setComplete(true);
                    }
                }
            }, getReactorExecutor());

        } catch(Exception e) {
            log.error("Failed to handle generic json command", e);
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }

    private ListeningExecutorService getReactorExecutor() {
        if(reactorExecutor == null){
            reactorExecutor = (ListeningScheduledExecutorService)this.controllerCatalog.getExecutionContext().getReactor().getExecutor();
        }
        return reactorExecutor;
    }

    private synchronized ListenableFuture<String> invoke(ControllerActionEntry entry, String payload,ControllerContext context) {
        if(entry.isSynchronous()){
            try(ControllerContext ctxt = ControllerContext.create(context)){
                return entry.invoke(payload,ctxt, getReactorExecutor());
            } catch (Exception e) {
                throw new RuntimeException(ControllerContext.Unwrap(e));
            }
        }
        try(ControllerContext ctxt = ControllerContext.create(context)){
            return entry.invoke(payload, ctxt,asyncExecutor);
        } catch (Exception e) {
            throw new RuntimeException(ControllerContext.Unwrap(e));
        }
    }


}

