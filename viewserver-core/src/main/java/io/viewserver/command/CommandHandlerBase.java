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

package io.viewserver.command;

import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.command.ICommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;

/**
 * Created by nick on 03/12/15.
 */
public abstract class CommandHandlerBase<TCommand> implements ICommandHandler {
    private Class<TCommand> clazz;

    protected CommandHandlerBase(Class<TCommand> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void handleCommand(Command command, IPeerSession peerSession) {
        CommandResult commandResult = CommandResult.get(command, peerSession);
        final ICommand commandMessage = (ICommand) command.getMessage();
        final IPoolableMessage extension = commandMessage.getExtension(clazz);
        handleCommand(command, (TCommand)extension, peerSession, commandResult);
    }

    protected abstract void handleCommand(Command command, TCommand data, IPeerSession peerSession, CommandResult commandResult);
}
