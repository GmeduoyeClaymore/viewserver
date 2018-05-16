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

import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bemm on 09/10/2014.
 */
public class CommandResult {
    private static final Logger log = LoggerFactory.getLogger(CommandResult.class);
    private String name;
    private int commandId;
    private boolean success;
    private boolean complete;
    private StringBuilder message = new StringBuilder();
    private ICommandResultListener listener;

    public CommandResult() {
    }

    public CommandResult(Command command) {
        this.commandId = command.getId();
    }

    public CommandResult(int commandId) {
        this.commandId = commandId;
    }

    public CommandResult(int commandId, ICommandResultListener listener) {
        this.commandId = commandId;
        this.listener = listener;
    }

    public CommandResult(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCommandId() {
        return commandId;
    }

    public boolean isSuccess() {
        return success;
    }

    public CommandResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        log.trace("Command result '{}' complete", name);
        this.complete = complete;
        if (listener != null) {
            listener.onResult(this);
        }
    }

    public String getMessage() {
        return message.toString();
    }

    public CommandResult setMessage(String message) {
        this.message.setLength(0);
        return addMessage(message);
    }

    public CommandResult addMessage(String message) {
        this.message.append(message);
        return this;
    }

    public ICommandResultListener getListener() {
        return listener;
    }

    public void setListener(ICommandResultListener listener) {
        this.listener = listener;
    }

    public static CommandResult get(Command command, IPeerSession peerSession) {
        CommandResult commandResult = new CommandResult(command);
        commandResult.setListener(peerSession::sendCommandResult);
        return commandResult;
    }
}


