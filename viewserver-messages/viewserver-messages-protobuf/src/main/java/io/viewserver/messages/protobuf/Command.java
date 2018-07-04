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

import com.google.protobuf.Extension;
import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.command.ICommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;

/**
 * Created by bemm on 02/12/15.
 */
public class Command extends PoolableMessage<Command> implements ICommand<Command> {
    private CommandMessage.CommandDtoOrBuilder commandDto;
    private ICommandExtension extension;

    public static Command fromDto(CommandMessage.CommandDto commandDto) {
        final Command command = (Command)MessagePool.getInstance().get(ICommand.class);
        command.commandDto = commandDto;
        return command;
    }

    Command() {
        super(ICommand.class);
    }

    @Override
    public int getId() {
        return commandDto.getId();
    }

    @Override
    public ICommand<Command> setId(int id) {
        getCommandDtoBuilder().setId(id);
        return this;
    }

    @Override
    public String getCommand() {
        return commandDto.getCommand();
    }

    @Override
    public ICommand<Command> setCommand(String command) {
        getCommandDtoBuilder().setCommand(command);
        return this;
    }

    @Override
    public <T extends IPoolableMessage> T getExtension(Class<T> clazz) {
        if (extension == null) {
            T extensionInstance = MessagePool.getInstance().get(clazz);
            if (!(extensionInstance instanceof ICommandExtension)) {
                extensionInstance.release();
                throw new IllegalArgumentException(String.format("Class %s is not a valid command extension - must implement %s",
                        clazz, ICommandExtension.class.getName()));
            }
            extension = (ICommandExtension)extensionInstance;
            Extension<CommandMessage.CommandDto,?> extension1 = CommandRegistry.INSTANCE.getExtension(clazz);
            if(extension1 == null){
                throw new IllegalArgumentException(String.format("Unable to find extension for class %s",
                        clazz));
            }
            extension.setDto(commandDto.getExtension(extension1));
        }
        return (T) extension;
    }

    @Override
    public <TMessage extends IPoolableMessage> ICommand<Command> setExtension(Class<TMessage> clazz, TMessage extension) {
        if (!(extension instanceof ICommandExtension)) {
            throw new IllegalArgumentException(String.format("Class %s is not a valid command extension - must implement %s",
                    clazz, ICommandExtension.class.getName()));
        }
        if (this.extension != null) {
            this.extension.release();
        }
        this.extension = (ICommandExtension) extension.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        if (extension != null) {
            extension.release();
            extension = null;
        }
        commandDto = null;
    }

    private CommandMessage.CommandDto.Builder getCommandDtoBuilder() {
        if (commandDto == null) {
            commandDto = CommandMessage.CommandDto.newBuilder();
        } else if (commandDto instanceof CommandMessage.CommandDto) {
            commandDto = ((CommandMessage.CommandDto) commandDto).toBuilder();
        }
        return (CommandMessage.CommandDto.Builder) commandDto;
    }

    CommandMessage.CommandDto.Builder getBuilder() {
        final CommandMessage.CommandDto.Builder commandDtoBuilder = getCommandDtoBuilder();
        if (extension != null) {
            extension.build(commandDtoBuilder);
        }
        return commandDtoBuilder;
    }
}
