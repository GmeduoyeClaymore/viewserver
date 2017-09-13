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
import io.viewserver.core.ExecutionContext;
import io.viewserver.messages.IMessage;
import io.viewserver.operators.deserialiser.DeserialiserOperator;

/**
 * Created by nickc on 07/10/2014.
 */
public interface IPeerSession {
    void sendCommand(Command command);

    void addOpenCommand(Command command);

    void removeOpenCommand(Command command);

    Command getOpenCommand(int commandId);

    void closeCommand(Command command);

    int getConnectionId();

    SessionType getSessionType();

    void setAuthenticationToken(AuthenticationToken authenticationToken);

    void sendMessage(IMessage message);

    void sendCommandResult(CommandResult commandResult);

    void removeDisconnectionHandler(PeerSession.IDisconnectionHandler disconnectionHandler);

    void fireDisconnection();

    ExecutionContext getExecutionContext();

    ICatalog getSessionCatalog();

    IMessageManager getMessageManager();

    void addDisconnectionHandler(PeerSession.IDisconnectionHandler disconnectionHandler);

    ICatalog getSystemCatalog();

    IChannel getChannel();

    Network getNetwork();

    int getNextCommandId();

    boolean isAuthenticated();

    AuthenticationToken getAuthenticationToken();

    void addAuthenticationEventHandler(IPeerSessionAuthenticationHandler handler);

    void removeAuthenticationEventHandler(IPeerSessionAuthenticationHandler handler);

    String getCatalogName();

    boolean shouldLog();

    void registerDeserialiser(DeserialiserOperator deserialiser);

    void unregisterDeserialiser(DeserialiserOperator deserialiser);

    boolean isTornDown();

    void tearDown();

    public enum SessionType {
        ServerToClient,
        ClientToServer
    }
}
