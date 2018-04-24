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
import io.viewserver.authentication.LoggerAuthenticationHandler;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;

/**
 * Created by bemm on 04/11/2014.
 */
public class ServerToClientSession extends PeerSession {
    private ICatalog sessionCatalog;

    public ServerToClientSession(IChannel channel, IExecutionContext executionContext, ICatalog systemCatalog,
                                 ICatalog sessionCatalog, Network network, int connectionId, IMessageManager messageManager) {
        super(channel, executionContext, systemCatalog, network, connectionId, messageManager);
        this.sessionCatalog = sessionCatalog;
    }

    @Override
    public SessionType getSessionType() {
        return SessionType.ServerToClient;
    }

    @Override
    public ICatalog getSessionCatalog() {
        return sessionCatalog;
    }

    @Override
    public String getCatalogName() {
        return getNetwork().getNetworkAdapter().getCatalogNameForChannel(getChannel());
    }

    @Override
    public boolean shouldLog() {
        AuthenticationToken authenticationToken = getAuthenticationToken();
        if (authenticationToken != null && authenticationToken.getType().equals(LoggerAuthenticationHandler.TYPE)) {
            return false;
        }
        return true;
    }

    @Override
    public void tearDown() {
        sessionCatalog.tearDown();

        super.tearDown();
    }
}
