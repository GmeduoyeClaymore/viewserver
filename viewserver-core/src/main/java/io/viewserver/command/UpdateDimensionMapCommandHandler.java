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

import io.viewserver.datasource.DimensionMapUpdater;
import io.viewserver.messages.command.IUpdateDimensionMapCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nick on 02/07/15.
 */
public class UpdateDimensionMapCommandHandler extends CommandHandlerBase<IUpdateDimensionMapCommand> {
    private static final Logger log = LoggerFactory.getLogger(UpdateDimensionMapCommandHandler.class);
    private DimensionMapUpdater dimensionMapUpdater;

    public UpdateDimensionMapCommandHandler(DimensionMapUpdater dimensionMapUpdater) {
        super(IUpdateDimensionMapCommand.class);
        this.dimensionMapUpdater = dimensionMapUpdater;
    }

    @Override
    protected void handleCommand(Command command, IUpdateDimensionMapCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            dimensionMapUpdater.update(data);
            commandResult.setSuccess(true).setComplete(true);
        } catch (Throwable e) {
            log.error("Failed to update dimension map", e);
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }
}
