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
import io.viewserver.messages.command.IRegisterSlaveCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.RegisterSlaveCommandMessage;

/**
 * Created by bemm on 07/12/15.
 */
public class RegisterSlaveCommand extends PoolableMessage<RegisterSlaveCommand> implements IRegisterSlaveCommand<RegisterSlaveCommand>,
ICommandExtension<RegisterSlaveCommand> {
    private RegisterSlaveCommandMessage.RegisterSlaveCommandDtoOrBuilder registerSlaveCommandDto;

    RegisterSlaveCommand() {
        super(IRegisterSlaveCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        registerSlaveCommandDto = (RegisterSlaveCommandMessage.RegisterSlaveCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(RegisterSlaveCommandMessage.registerSlaveCommand, getBuilder().buildPartial());
    }

    @Override
    protected void doRelease() {
        registerSlaveCommandDto = null;
    }

    RegisterSlaveCommandMessage.RegisterSlaveCommandDto.Builder getBuilder() {
        return getRegisterSlaveCommandDtoBuilder();
    }

    private RegisterSlaveCommandMessage.RegisterSlaveCommandDto.Builder getRegisterSlaveCommandDtoBuilder() {
        if (registerSlaveCommandDto == null) {
            registerSlaveCommandDto = RegisterSlaveCommandMessage.RegisterSlaveCommandDto.newBuilder();
        } else if (registerSlaveCommandDto instanceof RegisterSlaveCommandMessage.RegisterSlaveCommandDto) {
            registerSlaveCommandDto = ((RegisterSlaveCommandMessage.RegisterSlaveCommandDto) registerSlaveCommandDto).toBuilder();
        }
        return (RegisterSlaveCommandMessage.RegisterSlaveCommandDto.Builder) registerSlaveCommandDto;
    }
}
