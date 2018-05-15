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

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandHandlerRegistry;
import io.viewserver.command.CommandResult;
import io.viewserver.command.ICommandHandler;
import io.viewserver.core.IExecutionContext;
import io.viewserver.messages.IMessage;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.ICommand;
import io.viewserver.messages.command.ICommandResult;
import io.viewserver.messages.heartbeat.IHeartbeat;
import io.viewserver.messages.tableevent.ITableEvent;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;


public class Network implements PeerSession.IDisconnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(Network.class);
    private final ICatalog catalog;
    private final IExecutionContext executionContext;
    private int nextConnectionId = 0;
    private CommandHandlerRegistry commandHandlerRegistry;
    private IReactor reactor;
    private HeartbeatTask heartbeatTask;
    private TObjectIntHashMap<IChannel> connectionIds = new TObjectIntHashMap<>(8, 0.75f, -1);
    private final SessionManager sessionManager;
    private final INetworkAdapter networkAdapter;
    private boolean disconnectOnTimeout;
    private int timeoutInterval = 5000;
    private PublishSubject<IChannel> channelsConnected;

    public Network(CommandHandlerRegistry commandHandlerRegistry, IExecutionContext executionContext, ICatalog catalog,
                   INetworkAdapter networkAdapter) {
        this.commandHandlerRegistry = commandHandlerRegistry;
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.networkAdapter = networkAdapter;
        this.channelsConnected = PublishSubject.create();
        this.sessionManager = new SessionManager(executionContext, catalog);
    }

    public INetworkAdapter getNetworkAdapter() {
        return networkAdapter;
    }

    public CommandHandlerRegistry getCommandHandlerRegistry() {
        return commandHandlerRegistry;
    }

    public void listen(final IEndpoint endpoint) {
        log.info("Listening on {}", endpoint);
        reactor.scheduleTask(() -> networkAdapter.listen(endpoint), 0, -1);
    }

    public ListenableFuture<IPeerSession> connect(final IEndpoint endpoint) {
        SettableFuture<IPeerSession> future = SettableFuture.create();
        log.debug("Connecting to {}", endpoint);
        final int connectionId = getNextConnectionId();
        ITask connectTask = new ITask() {
            @Override
            public void execute() {
                ITask thisTask = this;
                ListenableFuture<IChannel> channelFuture = networkAdapter.connect(endpoint);
                reactor.addCallback(channelFuture, new FutureCallback<IChannel>() {
                    @Override
                    public void onSuccess(IChannel channel) {
                        ClientToServerSession peerSession = new ClientToServerSession(channel, executionContext, catalog,
                                Network.this, connectionId, networkAdapter.createMessageManager(channel));
                        peerSession.addDisconnectionHandler(Network.this);
                        connectionIds.put(channel, connectionId);
                        sessionManager.addSession(peerSession);
                        heartbeatTask.sessions.add(peerSession);
                        heartbeatTask.lastResponses.put(peerSession, System.currentTimeMillis());
                        log.debug("Connection {} initialised on channel {}", connectionId, channel);
                        future.set(peerSession);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error(String.format("Failed to connect to %s", endpoint), t);
                        reactor.scheduleTask(thisTask, 1000, -1);
                    }
                });
            }
        };
        reactor.scheduleTask(connectTask, 0, -1);
        log.debug("Scheduled task to connect to server");
        return future;
    }

    public void sendCommand(Command command, int connectionId) {
        IPeerSession peerSession = sessionManager.getSessionById(connectionId);
        if (peerSession == null) {
            log.warn("Trying to send command to unknown connection id {}", connectionId);
            command.onResult(false, "Invalid connection id");
            return;
        }

        command.setId(peerSession.getNextCommandId());
        command.setConnectionId(connectionId);
        peerSession.addOpenCommand(command);

        if (peerSession.shouldLog()) {
            log.debug("Sending command {}.{} - {}", connectionId, command.getId(), command.getCommand());
        }

        final IMessage message = MessagePool.getInstance().get(IMessage.class);
        final ICommand commandMessage = message.getCommand()
                .setId(command.getId())
                .setCommand(command.getCommand());
        if (command.getMessage() != null) {
            commandMessage.setExtension(command.getMessage().getClass(), command.getMessage());
        }
        peerSession.getMessageManager().sendMessage(message);
        message.release();
    }

    public void sendCommandResult(CommandResult commandResult, int connectionId) {
        IPeerSession peerSession = sessionManager.getSessionById(connectionId);
        if (peerSession == null) {
            log.warn("Trying to send command result to unknown connection id {}", connectionId);
            return;
        }
        final IMessage message = MessagePool.getInstance().get(IMessage.class);
        final ICommandResult commandResultMessage = message.getCommandResult()
                .setId(commandResult.getCommandId())
                .setSuccess(commandResult.isSuccess());
        if (commandResult.getMessage() != null) {
            commandResultMessage.setMessage(commandResult.getMessage());
        }
        peerSession.sendMessage(message);
        message.release();
    }

    public void reset() {
        networkAdapter.reset();
    }

    public void receiveCommand(ICommand commandMessage, IPeerSession peerSession) {
        ICommandHandler commandHandler = commandHandlerRegistry.get(commandMessage.getCommand());
        if (commandHandler == null) {
            log.warn("Received unknown command {}.{} - '{}'", peerSession.getConnectionId(), commandMessage.getId(), commandMessage.getCommand());
            return;
        }

        final Command command;
        command = new Command(commandMessage.getCommand(), commandMessage);
        command.setId(commandMessage.getId());

        // auth check disabled pending changes required to UI
//        if (peerSession instanceof ServerToClientSession && !AuthenticationHandlerRegistry.AUTHENTICATE_COMMAND.equals(command.getCommand())) {
//            if (!peerSession.isAuthenticated()) {
//                CommandResult commandResult = new CommandResult(command);
//                commandResult.setSuccess(false).setMessage("Not authenticated").setComplete(true);
//                sendCommandResult(commandResult, peerSession.getConnectionId());
//                return;
//            }
//
//            if (!peerSession.getAuthenticationToken().isCommandAuthorised(command.getCommand())) {
//                CommandResult commandResult = new CommandResult(command);
//                commandResult.setSuccess(false).setMessage("Not authorised").setComplete(true);
//                sendCommandResult(commandResult, peerSession.getConnectionId());
//                return;
//            }
//        }

        if (peerSession.shouldLog()) {
            log.trace("Received command {}.{} - {}", peerSession.getConnectionId(), command.getId(), command.getCommand());
        }
        commandHandler.handleCommand(command, peerSession);
    }

    private void receiveHeartbeat(IHeartbeat heartbeat, IPeerSession peerSession) {
        if (log.isTraceEnabled()) {
            log.trace("Received heartbeat from connection id {}", peerSession.getConnectionId());
        }
        if (heartbeat.getType().equals(IHeartbeat.Type.Ping)) {
            sendHeartbeat(peerSession, IHeartbeat.Type.Pong);
        } else {
            heartbeatTask.lastResponses.put(peerSession, System.currentTimeMillis());
        }
    }

    private void sendHeartbeat(IPeerSession peerSession, IHeartbeat.Type type) {
        if (peerSession == null) {
            log.error("PeerSession does not exist, cannot send heartbeat");
            return;
        }
        final IMessage message = MessagePool.getInstance().get(IMessage.class);
        message.getHeartbeat().setType(type);
        peerSession.sendMessage(message);
        message.release();
    }

    private void receiveTableEvent(ITableEvent tableEvent, IPeerSession peerSession) {
        log.debug("Received table event {}.{}", peerSession.getConnectionId(), tableEvent.getId());
        Command command = peerSession.getOpenCommand(tableEvent.getId());
        if (command == null) {
            log.warn("Received table event for non-existent or closed command id {}.{}", peerSession.getConnectionId(), tableEvent.getId());
            return;
        }

        if (command.getDataHandler() != null) {
            command.getDataHandler().handleTableEvent(tableEvent);
        }
    }

    private void receiveCommandResult(ICommandResult commandResult, IPeerSession peerSession) {
        Command command = peerSession.getOpenCommand(commandResult.getId());
        if (command == null) {
            log.warn("Received command result for non-existent or closed command id {}.{}", peerSession.getConnectionId(), commandResult.getId());
            return;
        }

        if (!command.isSilent()) {
            if (commandResult.isSuccess()) {
                log.debug("Received command result {}.{} - {}", peerSession.getConnectionId(), commandResult.getId(), commandResult.isSuccess());
            } else {
                log.warn("Received command result {}.{} - {} - {}", peerSession.getConnectionId(), commandResult.getId(), commandResult.isSuccess(), commandResult.getMessage());
            }
        }

        command.onResult(commandResult.isSuccess(), commandResult.getMessage());

        if (!commandResult.isSuccess() || !command.isContinuous()) {
            peerSession.removeOpenCommand(command);
        }
    }

    public void setReactor(IReactor reactor) {
        this.reactor = reactor;
        networkAdapter.setReactor(reactor);
        heartbeatTask = new HeartbeatTask();
        reactor.addLoopTask(heartbeatTask);
    }

    public IReactor getReactor() {
        return reactor;
    }

    public void handleConnect(IChannel channel) {
        int connectionId = connectionIds.get(channel);
        if (connectionId != connectionIds.getNoEntryValue()) {
            log.warn("Channel already connected");
            return;
        }

        connectionId = getNextConnectionId();
        connectionIds.put(channel, connectionId);
        ServerToClientSession peerSession = new ServerToClientSession(channel, executionContext, catalog,
                catalog.createDescendant("remotes/" + networkAdapter.getCatalogNameForChannel(channel)),
                this, connectionId, networkAdapter.createMessageManager(channel));
        peerSession.addDisconnectionHandler(this);
        sessionManager.addSession(peerSession);
        heartbeatTask.sessions.add(peerSession);
        this.channelsConnected.onNext(channel);
    }

    public boolean receiveMessage(IChannel channel, IMessage message) {
        IPeerSession peerSession = getiPeerSession(channel);
        log.trace("Received message - {}",message);
        boolean needCommit = false;
        switch (message.getType()) {
            case Heartbeat: {
                if (peerSession == null) {
                    log.warn("Received heartbeat from unknown connection on channel {}", channel);
                    return false;
                }
                receiveHeartbeat(message.getHeartbeat(), peerSession);
                break;
            }
            case Command: {
                if (peerSession == null) {
                    log.warn("Received command from unknown connection on channel {} - {} [id={}]", channel, message.getCommand().getCommand(), message.getCommand().getId());
                    return false;
                }
                receiveCommand(message.getCommand(), peerSession);
                needCommit = true;
                break;
            }
            case CommandResult: {
                if (peerSession == null) {
                    log.warn("Received command result for id {} from unknown connection on channel {}", message.getCommandResult().getId(), channel);
                    return false;
                }
                receiveCommandResult(message.getCommandResult(), peerSession);
                break;
            }
            case TableEvent: {
                if (peerSession == null) {
                    log.warn("Received table event {} from unknown connection on channel {}", message.getTableEvent().getId(), channel);
                    return false;
                }
                receiveTableEvent(message.getTableEvent(), peerSession);
                needCommit = true;
                break;
            }
        }

        heartbeatTask.lastResponses.put(peerSession, System.currentTimeMillis());
        heartbeatTask.lastPings.put(peerSession, -1);

        return needCommit;
    }

    public IPeerSession getiPeerSession(IChannel channel) {
        int connectionId = connectionIds.get(channel);
        return sessionManager.getSessionById(connectionId);
    }

    public rx.Observable<IPeerSession> waitForSession(IChannel channel){
        int connectionId = connectionIds.get(channel);
        if (connectionId != connectionIds.getNoEntryValue()) {
            return Observable.just(getiPeerSession(channel));
        }
        return this.channelsConnected.filter(ch-> matches(ch,channel)).map(ch -> getiPeerSession(channel)).take(1);
    }

    private boolean matches(IChannel ch, IChannel channel) {
        int connectionId = connectionIds.get(ch);
        if (connectionId != connectionIds.getNoEntryValue()) {
          return false;
        }
        int connectionId2 = connectionIds.get(channel);
        if (connectionId2 != connectionIds.getNoEntryValue()) {
            return false;
        }
        return connectionId == connectionId2;
    }

    private int getNextConnectionId() {
        return nextConnectionId++;
    }

    public void handleDisconnect(IChannel channel) {
        int connectionId = connectionIds.get(channel);
        if (connectionId == connectionIds.getNoEntryValue()) {
            log.warn("Handling disconnect for non-existent channel");
            return;
        }
        IPeerSession peerSession = sessionManager.getSessionById(connectionId);
        peerSession.fireDisconnection();
    }

    @Override
    public void handleDisconnect(IPeerSession peerSession) {
        sessionManager.removeSession(peerSession);
        connectionIds.remove(peerSession.getChannel());
        heartbeatTask.removeSession(peerSession);

        peerSession.tearDown();
    }

    public void shutdown() {
        networkAdapter.shutdown();
    }

    public void setDisconnectOnTimeout(boolean disconnectOnTimeout) {
        this.disconnectOnTimeout = disconnectOnTimeout;
    }
    public void setTimeoutInterval(int timeoutInterval) {
        this.timeoutInterval = timeoutInterval;
    }

    private class HeartbeatTask implements Runnable {
        private final List<IPeerSession> sessions = new ArrayList<>();
        private final List<IPeerSession> sessionsCopy = new ArrayList<>();
        private TObjectLongHashMap<IPeerSession> lastResponses = new TObjectLongHashMap<>(8, 0.75f, -1);
        private TObjectLongHashMap<IPeerSession> lastPings = new TObjectLongHashMap<>(8, 0.75f, -1);

        @Override
        public void run() {
            ArrayList<IPeerSession> sessions;
            synchronized (this.sessions) {
                sessionsCopy.clear();
                sessionsCopy.addAll(this.sessions);
            }
            int count = sessionsCopy.size();
            for (int i = 0; i < count; i++) {
                IPeerSession peerSession = sessionsCopy.get(i);
                long now = System.currentTimeMillis();
                long lastPing = lastPings.get(peerSession);
                long lastResponse = lastResponses.get(peerSession);
                if (lastPing > -1) {
                    if (now - lastPing > (timeoutInterval) ) {
                        // timeout!
                        log.debug("Session {} timed out!", peerSession.getConnectionId());
                        // TODO: correctly handle client-to-server sessions
                        if (peerSession instanceof ServerToClientSession) {
                            if(disconnectOnTimeout){
                                peerSession.fireDisconnection();
                            }
                            else{
                                log.debug("DISCONNECT DISABLED FOR DEV!! Session {} timed out!", peerSession.getConnectionId());
                            }
                        }
                    }
                    continue;
                }

                long elapsed = now - lastResponse;
                if (elapsed >= interval) {
                    lastPings.put(peerSession, now);
                    sendHeartbeat(peerSession, IHeartbeat.Type.Ping);
                }
            }
        }

        private void removeSession(IPeerSession peerSession) {
            synchronized (sessions) {
                sessions.remove(peerSession);
                lastPings.remove(peerSession);
                lastResponses.remove(peerSession);
            }
        }
    }
}
