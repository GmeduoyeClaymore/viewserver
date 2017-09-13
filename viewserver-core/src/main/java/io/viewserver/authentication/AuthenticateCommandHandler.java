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

package io.viewserver.authentication;

import io.viewserver.command.CommandHandlerBase;
import io.viewserver.command.CommandResult;
import io.viewserver.messages.command.IAuthenticateCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;

/**
 * Created by nick on 27/10/15.
 */
public class AuthenticateCommandHandler extends CommandHandlerBase<IAuthenticateCommand> {
    private final AuthenticationHandlerRegistry authenticationHandlerRegistry;

    public AuthenticateCommandHandler(AuthenticationHandlerRegistry authenticationHandlerRegistry) {
        super(IAuthenticateCommand.class);
        this.authenticationHandlerRegistry = authenticationHandlerRegistry;
    }

    @Override
    protected void handleCommand(Command command, IAuthenticateCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            IAuthenticationHandler authenticationHandler = authenticationHandlerRegistry.get(data.getType());
            if (authenticationHandler == null) {
                throw new Exception("Invalid authentication type '" + data.getType() + "'");
            }

            AuthenticationToken authenticationToken = authenticationHandler.authenticate(data);
            if (authenticationToken == null) {
                throw new Exception("Unknown authentication error");
            }

            peerSession.setAuthenticationToken(authenticationToken);
            commandResult.setSuccess(true).setComplete(true);
        } catch (Throwable e) {
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }
}
