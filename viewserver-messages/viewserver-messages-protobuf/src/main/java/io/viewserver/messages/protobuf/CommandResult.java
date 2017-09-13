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

import io.viewserver.messages.MessagePool;
import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.command.ICommandResult;
import io.viewserver.messages.protobuf.dto.CommandResultMessage;

/**
 * Created by nick on 02/12/15.
 */
public class CommandResult extends PoolableMessage<CommandResult> implements ICommandResult<CommandResult> {
    private CommandResultMessage.CommandResultDtoOrBuilder commandResultDto;

    public static CommandResult fromDto(CommandResultMessage.CommandResultDto commandResultDto) {
        final CommandResult commandResult = (CommandResult)MessagePool.getInstance().get(ICommandResult.class);
        commandResult.commandResultDto = commandResultDto;
        return commandResult;
    }

    CommandResult() {
        super(ICommandResult.class);
    }

    @Override
    public int getId() {
        return commandResultDto.getId();
    }

    @Override
    public ICommandResult<CommandResult> setId(int id) {
        getCommandResultDtoBuilder().setId(id);
        return this;
    }

    @Override
    public boolean isSuccess() {
        return commandResultDto.getSuccess();
    }

    @Override
    public ICommandResult<CommandResult> setSuccess(boolean success) {
        getCommandResultDtoBuilder().setSuccess(success);
        return this;
    }

    @Override
    public String getMessage() {
        return commandResultDto.hasMessage() ? commandResultDto.getMessage() : null;
    }

    @Override
    public ICommandResult<CommandResult> setMessage(String message) {
        getCommandResultDtoBuilder().setMessage(message);
        return this;
    }

    @Override
    protected void doRelease() {
        commandResultDto = null;
    }

    private CommandResultMessage.CommandResultDto.Builder getCommandResultDtoBuilder() {
        if (commandResultDto == null) {
            commandResultDto = CommandResultMessage.CommandResultDto.newBuilder();
        } else if (commandResultDto instanceof CommandResultMessage.CommandResultDto) {
            commandResultDto = ((CommandResultMessage.CommandResultDto) commandResultDto).toBuilder();
        }
        return (CommandResultMessage.CommandResultDto.Builder) commandResultDto;
    }

    CommandResultMessage.CommandResultDto.Builder getBuilder() {
        return getCommandResultDtoBuilder();
    }
}
