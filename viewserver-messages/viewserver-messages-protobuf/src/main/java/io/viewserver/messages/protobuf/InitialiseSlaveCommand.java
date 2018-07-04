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
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.InitialiseSlaveCommandMessage;

/**
 * Created by bemm on 07/12/15.
 */
public class InitialiseSlaveCommand extends PoolableMessage<InitialiseSlaveCommand> implements IInitialiseSlaveCommand<InitialiseSlaveCommand>,
        ICommandExtension<InitialiseSlaveCommand> {
    private InitialiseSlaveCommandMessage.InitialiseSlaveCommandDtoOrBuilder initialiseSlaveCommandDto;

    InitialiseSlaveCommand() {
        super(IInitialiseSlaveCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        initialiseSlaveCommandDto = (InitialiseSlaveCommandMessage.InitialiseSlaveCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(InitialiseSlaveCommandMessage.initialiseSlaveCommand, getBuilder().buildPartial());
    }

    @Override
    public Type getType() {
        final InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.SlaveType type = initialiseSlaveCommandDto.getType();
        switch (type) {
            case MASTER: {
                return Type.Master;
            }
            case AGGREGATOR: {
                return Type.Aggregator;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown slave type '%s'", type));
            }
        }
    }

    @Override
    public IInitialiseSlaveCommand<InitialiseSlaveCommand> setType(Type type) {
        switch (type) {
            case Master: {
                getInitialiseSlaveCommandDtoBuilder().setType(InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.SlaveType.MASTER);
                break;
            }
            case Aggregator: {
                getInitialiseSlaveCommandDtoBuilder().setType(InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.SlaveType.AGGREGATOR);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown slave type '%s'", type));
            }
        }
        return this;
    }

    @Override
    protected void doRelease() {
        initialiseSlaveCommandDto = null;
    }

    InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.Builder getBuilder() {
        return getInitialiseSlaveCommandDtoBuilder();
    }

    private InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.Builder getInitialiseSlaveCommandDtoBuilder() {
        if (initialiseSlaveCommandDto == null) {
            initialiseSlaveCommandDto = InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.newBuilder();
        } else if (initialiseSlaveCommandDto instanceof InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto) {
            initialiseSlaveCommandDto = ((InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto) initialiseSlaveCommandDto).toBuilder();
        }
        return (InitialiseSlaveCommandMessage.InitialiseSlaveCommandDto.Builder) initialiseSlaveCommandDto;
    }
}
