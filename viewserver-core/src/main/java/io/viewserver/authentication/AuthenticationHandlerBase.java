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

import io.viewserver.messages.command.IAuthenticateCommand;

import java.util.List;

/**
 * Created by nick on 13/02/2015.
 */
public abstract class AuthenticationHandlerBase implements IAuthenticationHandler {
    @Override
    public final AuthenticationToken authenticate(IAuthenticateCommand authenticateCommandDto) {
        AuthenticationToken token = new AuthenticationToken(authenticateCommandDto.getType(), authenticate(authenticateCommandDto.getClientVersion()));
        authorise(token);
        return token;
    }

    protected abstract String authenticate(String clientVersion);

    protected void authorise(AuthenticationToken token) {
    }
}
