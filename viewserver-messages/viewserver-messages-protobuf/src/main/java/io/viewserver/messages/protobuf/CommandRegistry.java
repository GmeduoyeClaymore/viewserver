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
import com.google.protobuf.ExtensionRegistry;
import io.viewserver.messages.command.*;
import io.viewserver.messages.protobuf.dto.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bemm on 02/12/15.
 */
public class CommandRegistry {
    public static final CommandRegistry INSTANCE = new CommandRegistry();
    private final Map<Class, Extension<CommandMessage.CommandDto, ?>> registry = new HashMap<>();
    private final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();

    public CommandRegistry() {
        registerExtension(IAuthenticateCommand.class, AuthenticateCommandMessage.authenticateCommand);
        registerExtension(ISubscribeCommand.class, SubscribeCommandMessage.subscribeCommand);
        registerExtension(ISubscribeDataSourceCommand.class, SubscribeDataSourceCommandMessage.subscribeDataSourceCommand);
        registerExtension(ISubscribeReportCommand.class, SubscribeReportCommandMessage.subscribeReportCommand);
        registerExtension(ISubscribeDimensionCommand.class, SubscribeDimensionCommandMessage.subscribeDimensionCommand);
        registerExtension(IUpdateSubscriptionCommand.class, UpdateSubscriptionCommandMessage.updateSubscriptionCommand);
        registerExtension(IUnsubscribeCommand.class, UnsubscribeCommandMessage.unsubscribeCommand);
        registerExtension(ITableEditCommand.class, TableEditCommandMessage.tableEditCommand);
        registerExtension(IRegisterSlaveCommand.class, RegisterSlaveCommandMessage.registerSlaveCommand);
        registerExtension(IInitialiseSlaveCommand.class, InitialiseSlaveCommandMessage.initialiseSlaveCommand);
        registerExtension(IUpdateDimensionMapCommand.class, UpdateDimensionMapCommandMessage.updateDimensionMapCommand);
        registerExtension(IRegisterDataSourceCommand.class, RegisterDataSourceCommandMessage.registerDataSourceCommand);
        registerExtension(IConfigurateCommand.class, ConfiguratorMessage.configurator);
        registerExtension(IGenericJSONCommand.class, GenericJSONCommandMessage.genericJSONCommand);
    }

    public Extension<CommandMessage.CommandDto, ?> getExtension(Class clazz) {
        return registry.get(clazz);
    }

    public void registerExtension(Class clazz, Extension<CommandMessage.CommandDto, ?> extension) {
        registry.put(clazz, extension);
        extensionRegistry.add(extension);
    }

    public ExtensionRegistry getExtensionRegistry() {
        return extensionRegistry;
    }
}
