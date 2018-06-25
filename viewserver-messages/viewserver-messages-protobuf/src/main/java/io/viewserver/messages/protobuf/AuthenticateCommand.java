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

package io.viewserver.messages.protobuf;

import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.command.IAuthenticateCommand;
import io.viewserver.messages.protobuf.dto.AuthenticateCommandMessage;
import io.viewserver.messages.protobuf.dto.CommandMessage;

/**
 * Created by nick on 02/12/15.
 */
public class AuthenticateCommand extends PoolableMessage<AuthenticateCommand> implements IAuthenticateCommand<AuthenticateCommand>,
        ICommandExtension<AuthenticateCommand> {
    private AuthenticateCommandMessage.AuthenticateCommandDtoOrBuilder authenticateCommandDto;

    AuthenticateCommand() {
        super(IAuthenticateCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        this.authenticateCommandDto = (AuthenticateCommandMessage.AuthenticateCommandDto) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(AuthenticateCommandMessage.authenticateCommand, getAuthenticateCommandDtoBuilder().buildPartial());
    }

    @Override
    public String getType() {
        return authenticateCommandDto.getType();
    }

    @Override
    public IAuthenticateCommand setType(String type) {
        getAuthenticateCommandDtoBuilder().setType(type);
        return this;
    }

    @Override
    public IAuthenticateCommand setClientVersion(String clientVersion) {
        getAuthenticateCommandDtoBuilder().setClientVersion(clientVersion);
        return this;
    }

    @Override
    public String getClientVersion() {
       return authenticateCommandDto != null ? authenticateCommandDto.getClientVersion() : null;
    }

    @Override
    protected void doRelease() {
        authenticateCommandDto = null;
    }

    private AuthenticateCommandMessage.AuthenticateCommandDto.Builder getAuthenticateCommandDtoBuilder() {
        if (authenticateCommandDto == null) {
            authenticateCommandDto = AuthenticateCommandMessage.AuthenticateCommandDto.newBuilder();
        } else if (authenticateCommandDto instanceof AuthenticateCommandMessage.AuthenticateCommandDto) {
            authenticateCommandDto = ((AuthenticateCommandMessage.AuthenticateCommandDto) authenticateCommandDto).toBuilder();
        }
        return (AuthenticateCommandMessage.AuthenticateCommandDto.Builder) authenticateCommandDto;
    }
}
