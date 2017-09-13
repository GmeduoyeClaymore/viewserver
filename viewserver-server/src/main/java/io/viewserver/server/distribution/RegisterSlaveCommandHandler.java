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

package io.viewserver.server.distribution;

import io.viewserver.command.CommandHandlerBase;
import io.viewserver.command.CommandResult;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.messages.command.IRegisterSlaveCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 25/11/2014.
 */
public class RegisterSlaveCommandHandler extends CommandHandlerBase<IRegisterSlaveCommand> {
    private static final Logger log = LoggerFactory.getLogger(RegisterSlaveCommandHandler.class);
    private IDistributionManager distributionManager;

    public RegisterSlaveCommandHandler(IDistributionManager distributionManager) {
        super(IRegisterSlaveCommand.class);
        this.distributionManager = distributionManager;
    }

    @Override
    protected void handleCommand(Command command, IRegisterSlaveCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            distributionManager.addNode(peerSession, commandResult);
        }
        catch (Throwable ex) {
            log.error("Error while registering slave", ex);
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
        }
    }
}
