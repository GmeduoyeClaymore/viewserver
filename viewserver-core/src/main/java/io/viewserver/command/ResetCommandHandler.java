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

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.ExecutionContext;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.datasource.IDimensionMapper;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 21/11/2014.
 */
public class ResetCommandHandler implements ICommandHandler {
    private static final Logger log = LoggerFactory.getLogger(ResetCommandHandler.class);
    private final Resetter resetter;

    public ResetCommandHandler(IDataSourceRegistry dataSourceRegistry,
                               IDistributionManager distributionManager,
                               ExecutionContext executionContext,
                               ICatalog systemCatalog,
                               IDimensionMapper dimensionMapper) {
        resetter = new Resetter(systemCatalog, dataSourceRegistry, distributionManager, executionContext, dimensionMapper);
    }

    @Override
    public void handleCommand(Command command, IPeerSession peerSession) {
        CommandResult commandResult = CommandResult.get(command, peerSession);

        resetter.reset(commandResult, false);
    }
}
