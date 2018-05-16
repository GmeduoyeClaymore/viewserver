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

package io.viewserver.messages;

import io.viewserver.messages.command.ICommand;
import io.viewserver.messages.command.ICommandResult;
import io.viewserver.messages.heartbeat.IHeartbeat;
import io.viewserver.messages.tableevent.ITableEvent;

/**
 * Created by nick on 02/12/15.
 */
public interface IMessage<T> extends IPoolableMessage<T> {
    Type getType();
    IHeartbeat getHeartbeat();

    IMessage setHeartbeat(IHeartbeat heartbeat);

    ICommand getCommand();

    IMessage setCommand(ICommand command);

    ICommandResult getCommandResult();

    IMessage setCommandResult(ICommandResult commandResult);

    ITableEvent getTableEvent();

    IMessage setTableEvent(ITableEvent tableEvent);

    enum Type {
        Heartbeat,
        Command,
        CommandResult,
        TableEvent
    }
}
