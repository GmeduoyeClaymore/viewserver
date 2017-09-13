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

package io.viewserver.network;

import io.viewserver.command.CommandResult;
import io.viewserver.command.ICommandResultListener;
import io.viewserver.messages.IPoolableMessage;

/**
 * Created by nickc on 08/10/2014.
 */
public class Command {
    private IPoolableMessage message;
    private int id;
    private final String command;
    private boolean continuous;
    private ICommandResultListener commandResultListener;
    private IDataHandler dataHandler;
    private int connectionId;
    private boolean resultSent;
    private boolean silent;

    public Command(String command) {
        this.command = command;
    }

    public Command(String command, IPoolableMessage message) {
        this.command = command;
        this.message = message;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public int getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public IPoolableMessage getMessage() {
        return message;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void onResult(boolean success, String message) {
        if (resultSent) {
            return;
        }
        resultSent = true;
        if (commandResultListener != null) {
            CommandResult commandResult = new CommandResult(this);
            commandResult.setSuccess(success)
                    .setMessage(message)
                    .setComplete(true);
            commandResultListener.onResult(commandResult);
        }
    }

    public ICommandResultListener getCommandResultListener() {
        return commandResultListener;
    }

    public void setCommandResultListener(ICommandResultListener commandResultListener) {
        this.commandResultListener = commandResultListener;
    }

    public void setDataHandler(IDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public IDataHandler getDataHandler() {
        return dataHandler;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }
}
