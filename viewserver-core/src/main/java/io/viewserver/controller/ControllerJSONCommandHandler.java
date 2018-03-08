package io.viewserver.controller;

import com.google.common.util.concurrent.*;
import io.viewserver.command.CommandHandlerBase;
import io.viewserver.command.CommandResult;
import io.viewserver.messages.command.IGenericJSONCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * Created by Gbemiga on 12/12/17.
 */

public class ControllerJSONCommandHandler extends CommandHandlerBase<IGenericJSONCommand> {
    private static final Logger log = LoggerFactory.getLogger(ControllerJSONCommandHandler.class);

    private ListeningExecutorService asyncExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("controller-command-handler-%d").build()));
    private ListeningExecutorService currentThreadExecutor = MoreExecutors.newDirectExecutorService();
    public ControllerJSONCommandHandler() {
        super(IGenericJSONCommand.class);
    }
    private HashMap<String,ControllerRegistration> registrationHashMap = new HashMap<>();

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
            ControllerRegistration registration = registrationHashMap.get(controllerName);
            if(registration == null){
                throw new RuntimeException("Unable to find registration for controller named \"" + controllerName + "\"");
            }
            log.info(String.format("JSON command controller:\"%s\" action:\"%s\" payload:\"%s\"",controllerName,action,data.getPayload()));
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
                        commandResult.setSuccess(true).setMessage(invoke.get()).setComplete(true);
                    } catch (InterruptedException e) {
                        log.error("Failed to handle generic json command", e);
                        commandResult.setSuccess(false).setMessage(ControllerContext.Unwrap(e).getMessage()).setComplete(true);
                    } catch (ExecutionException e) {
                        log.error("Failed to handle generic json command", e);
                        commandResult.setSuccess(false).setMessage(ControllerContext.Unwrap(e).getMessage()).setComplete(true);
                    }
                }
            }, currentThreadExecutor);

        } catch(Exception e) {
            log.error("Failed to handle generic json command", e);
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }

    private synchronized ListenableFuture<String> invoke(ControllerActionEntry entry, String payload,ControllerContext context) {
        if(entry.isSynchronous()){
            try(ControllerContext ctxt = ControllerContext.create(context)){
                return entry.invoke(payload,ctxt,currentThreadExecutor);
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

    public void registerController(Object controller) {
        ControllerRegistration reg = new ControllerRegistration(controller);
        if(registrationHashMap.containsKey(reg.getName())){
            throw new RuntimeException("Already have a controller registered for name \"" + reg.getName() + "\"");
        }
        registrationHashMap.put(reg.getName(), reg);
    }
}

