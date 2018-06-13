/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.network;

import io.viewserver.authentication.AuthenticationToken;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.messages.IMessage;
import io.viewserver.operators.deserialiser.DeserialiserOperator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by bemm on 07/10/2014.
 */
public abstract class PeerSession implements IPeerSession {
    private static final Logger log = LoggerFactory.getLogger(PeerSession.class);
    protected final IMessageManager messageManager;
    private TIntObjectHashMap<Command> openCommands = new TIntObjectHashMap<>();
    private final int connectionId;
    private final IExecutionContext executionContext;
    private final ICatalog systemCatalog;
    private Set<IDisconnectionHandler> disconnectionHandlers;
    private final List<IDisconnectionHandler> disconnectionHandlersCopy = new ArrayList<>();
    private final IChannel channel;
    private final Network network;
    private AuthenticationToken authenticationToken;
    private Set<IPeerSessionAuthenticationHandler> authenticationHandlers;
    private final Set<DeserialiserOperator> deserialisers = new HashSet<>();
    private boolean isTornDown;
    private int nextCommandId = 0;
    private Subject disconnectSubject;

    protected PeerSession(IChannel channel, IExecutionContext  executionContext, ICatalog systemCatalog, Network network, int connectionId,
                          IMessageManager messageManager) {
        this.channel = channel;
        this.network = network;
        this.executionContext = executionContext;
        this.systemCatalog = systemCatalog;
        this.connectionId = connectionId;
        this.messageManager = messageManager;
        this.disconnectSubject = PublishSubject.create();
        messageManager.setPeerSession(this);
    }

    @Override
    public int getNextCommandId() {
        return nextCommandId++;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticationToken != null;
    }

    @Override
    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    @Override
    public void setAuthenticationToken(AuthenticationToken authenticationToken) {
        this.authenticationToken = authenticationToken;
        fireAuthentication();
    }

    @Override
    public void sendMessage(IMessage message) {
        messageManager.sendMessage(message);
    }

    @Override
    public void sendCommandResult(CommandResult commandResult) {
        network.sendCommandResult(commandResult, connectionId);
    }

    @Override
    public void sendCommand(Command command) { network.sendCommand(command, connectionId); }

    @Override
    public void addOpenCommand(Command command) {
        openCommands.put(command.getId(), command);
    }

    @Override
    public void removeOpenCommand(Command command) {
        openCommands.remove(command.getId());
    }

    @Override
    public Command getOpenCommand(int commandId) {
        return openCommands.get(commandId);
    }

    @Override
    public void closeCommand(Command command) {
        if (openCommands.remove(command.getId()) == null) {
            log.warn("Tried to close command {}.{} that wasn't open", connectionId, command.getId());
        }
    }

    @Override
    public int getConnectionId() {
        return connectionId;
    }

    @Override
    public IMessageManager getMessageManager() {
        return messageManager;
    }

    public IChannel getChannel() {
        return channel;
    }

    @Override
    public void addDisconnectionHandler(IDisconnectionHandler disconnectionHandler) {
        if (disconnectionHandlers == null) {
            disconnectionHandlers = new HashSet<>();
        }
        disconnectionHandlers.add(disconnectionHandler);
    }

    @Override
    public void removeDisconnectionHandler(IDisconnectionHandler disconnectionHandler) {
        if (disconnectionHandlers == null) {
            return;
        }
        disconnectionHandlers.remove(disconnectionHandler);
    }

    @Override
    public void fireDisconnection() {
        Set<IDisconnectionHandler> disconnectionHandlers = this.disconnectionHandlers;
        disconnectSubject.onNext(null);
        if (disconnectionHandlers != null) {
            disconnectionHandlersCopy.addAll(disconnectionHandlers);
            int count = disconnectionHandlersCopy.size();
            for (int i = 0; i < count; i++) {
                try {
                    disconnectionHandlersCopy.get(i).handleDisconnect(this);
                }catch (Throwable ex){
                    log.error(String.format("Problem firing disconnection  - %s",ex.getMessage()));
                }
            }
            disconnectionHandlersCopy.clear();
        }

    }

    @Override
    public void addAuthenticationEventHandler(IPeerSessionAuthenticationHandler handler) {
        if (authenticationHandlers == null) {
            authenticationHandlers = new HashSet<>();
        }
        authenticationHandlers.add(handler);
    }

    @Override
    public void removeAuthenticationEventHandler(IPeerSessionAuthenticationHandler handler) {
        if (authenticationHandlers == null) {
            return;
        }
        authenticationHandlers.remove(handler);
    }

    private void fireAuthentication() {
        if (authenticationHandlers != null) {
            for (IPeerSessionAuthenticationHandler authenticationHandler : authenticationHandlers) {
                authenticationHandler.handle(this, authenticationToken);
            }
        }
    }


    @Override
    public Observable onDisconnect() {
        return disconnectSubject;
    }

    @Override
    public IExecutionContext  getExecutionContext() {
        return executionContext;
    }

    @Override
    public ICatalog getSystemCatalog() {
        return systemCatalog;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void registerDeserialiser(DeserialiserOperator deserialiser) {
        deserialisers.add(deserialiser);
    }

    @Override
    public void unregisterDeserialiser(DeserialiserOperator deserialiser) {
        deserialisers.remove(deserialiser);
    }

    @Override
    public boolean isTornDown() {
        return isTornDown;
    }

    @Override
    public void tearDown() {
        if (isTornDown) {
            return;
        }
        isTornDown = true;

        List<DeserialiserOperator> deserialisersCopy = new ArrayList<>(deserialisers);
        int count = deserialisersCopy.size();
        for (int i = 0; i < count; i++) {
            deserialisersCopy.get(i).tearDown();
        }

        openCommands.forEachEntry(new TIntObjectProcedure<Command>() {
            @Override
            public boolean execute(int id, Command command) {
                command.onResult(false, "Session torn down");
                return true;
            }
        });
        openCommands.clear();

        authenticationHandlers.clear();
        disconnectionHandlers.clear();

        try {
            channel.close();
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
    }

    public interface IDisconnectionHandler {
        void handleDisconnect(IPeerSession peerSession);
    }

    @Override
    public String toString() {
        return channel.toString();
    }
}
