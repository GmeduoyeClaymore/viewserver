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

import io.viewserver.configurator.Configurator;
import io.viewserver.configurator.ProtoConfiguratorSpec;
import io.viewserver.messages.command.IConfigurateCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;

/**
 * Created by bemm on 25/11/2014.
 */
public class ConfigurateCommandHandler extends CommandHandlerBase<IConfigurateCommand> {
    public ConfigurateCommandHandler() {
        super(IConfigurateCommand.class);
    }

    @Override
    protected void handleCommand(Command command, IConfigurateCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            Configurator configurator = peerSession.getExecutionContext().getConfigurator();

            configurator.process(new ProtoConfiguratorSpec(data, peerSession.getExecutionContext().getOperatorFactoryRegistry()),
                    peerSession.getExecutionContext(), peerSession.getSystemCatalog(), commandResult);
        } catch (Throwable e) {
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }
}
