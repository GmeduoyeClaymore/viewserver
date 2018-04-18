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

import java.util.HashMap;

/**
 * Created by nickc on 07/10/2014.
 */
public class CommandHandlerRegistry implements ICommandHandlerRegistry {
    private static final Logger log = LoggerFactory.getLogger(CommandHandlerRegistry.class);
    private final HashMap<String, ICommandHandler> handlers = new HashMap<>();

    public void register(String command, ICommandHandler handler) {
        handlers.put(command, handler);
    }

    @Override
    public ICommandHandler get(String command) {
        return handlers.get(command);
    }
}
