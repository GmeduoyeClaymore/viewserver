package io.viewserver.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.*;
import io.viewserver.messages.command.IGenericJSONCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Gbemiga on 12/12/17.
 */
public class ControllerJSONCommandHandler extends CommandHandlerBase<IGenericJSONCommand>{
    private static final Logger log = LoggerFactory.getLogger(ControllerJSONCommandHandler.class);

    private ListeningExecutorService asyncExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("controller-command-handler-%d").build()));
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
            ControllerActionEntry entry = registration.getActions().get(data.getAction());
            if(entry == null){
                throw new RuntimeException("Unable to find action named \"" + data.getAction() + "\" in controller named \"" + controllerName + "\" containing actions \"" + String.join(",",registration.getActions().keySet()) + "\"" );
            }
            String payload = data.getPayload();
            ListenableFuture<String> invoke = invoke(entry, payload);
            invoke.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        commandResult.setSuccess(true).setMessage(invoke.get()).setComplete(true);
                    } catch (InterruptedException e) {
                        commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
                    } catch (ExecutionException e) {
                        commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
                    }
                }
            }, MoreExecutors.sameThreadExecutor());

        } catch(Exception e) {
            log.error("Failed to handle generic json command", e);
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }

    private ListenableFuture<String> invoke(ControllerActionEntry entry, String payload) {
        if(entry.isSynchronous()){
            return Futures.immediateFuture(entry.invoke(payload));
        }
        return asyncExecutor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return entry.invoke(payload);
            }
        });
    }

    public void registerController(Object controller) {
        ControllerRegistration reg = new ControllerRegistration(controller);
        if(registrationHashMap.containsKey(reg.getName())){
            throw new RuntimeException("Already have a controller registered for name \"" + reg.getName() + "\"");
        }
        registrationHashMap.put(reg.getName(), reg);
    }
}

