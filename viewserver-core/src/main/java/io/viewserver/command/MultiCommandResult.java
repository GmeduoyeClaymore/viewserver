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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bemm on 05/12/2014.
 */
public class MultiCommandResult extends CommandResult {
    private static final Logger log = LoggerFactory.getLogger(MultiCommandResult.class);
    private String name;
    private int failureCount = 0;
    private int commandCount = 0;
    private int completeCount = 0;
    private List<CommandResult> dependencyResults = new ArrayList<>();

    public MultiCommandResult(ICommandResultListener listener) {
        this(null, listener);
    }

    public MultiCommandResult(String name, ICommandResultListener listener) {
        super(-1, listener);
        this.name = name;
    }

    public void incrementCommandCount() {
        commandCount++;
        log.trace("MultiCommandResult '{}' - command count incremented to {}", name, commandCount);
    }

    @Override
    public CommandResult setSuccess(boolean success) {
        if (!success) {
            failureCount++;
            log.trace("MultiCommandResult '{}' - failure count incremented to {}", name, failureCount);
        }

        super.setSuccess(failureCount == 0);

        return this;
    }

    @Override
    public void setComplete(boolean complete) {
        if (++completeCount == commandCount) {
            log.trace("MultiCommandResult '{}' - complete count incremented to {} - triggering parent", name, completeCount);
            super.setComplete(true);
        } else if (completeCount > commandCount) {
            log.trace("MultiCommandResult '{}' - received {} complete, expected {} - not triggering parent", name, completeCount, commandCount);
        } else {
            log.trace("MultiCommandResult '{}' - complete count incremented to {} ({} required)", name, completeCount, commandCount);
        }
    }

    public CommandResult getResultForDependency(String dependencyName) {
        commandCount++;
        log.trace("MultiCommandResult '{}' - dependency '{}' added", name, dependencyName);
        CommandResult dependencyResult = new CommandResult(String.format("%s/%s", name, dependencyName));
        dependencyResult.setListener(commandResult -> {
            if (commandResult.isSuccess()) {
                log.trace("MultiCommandResult '{}' - dependency '{}' succeeded", name, dependencyName);
            } else {
                log.trace("MultiCommandResult '{}' - dependency '{}' failed", name, dependencyName);
            }
            MultiCommandResult.this.setSuccess(commandResult.isSuccess())
                    .setMessage(commandResult.getMessage())
                    .setComplete(true);
        });
        dependencyResults.add(dependencyResult);

        return dependencyResult;
    }

    public static MultiCommandResult wrap(String name, CommandResult commandResult) {
        return new MultiCommandResult(String.format("%s/%s", commandResult.getName(), name), executionResult -> {
            commandResult.setSuccess(executionResult.isSuccess())
                    .setMessage(executionResult.getMessage())
                    .setComplete(true);
        });
    }
}
