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
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.datasource.DimensionMapUpdater;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.datasource.SlaveDimensionMapper;
import io.viewserver.distribution.DistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.network.PeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 25/11/2014.
 */
public class InitialiseSlaveCommandHandler extends CommandHandlerBase<IInitialiseSlaveCommand> {
    private static final Logger log = LoggerFactory.getLogger(InitialiseSlaveCommandHandler.class);
    private final CommandHandlerRegistry commandHandlerRegistry;
    private final IDataSourceRegistry dataSourceRegistry;
    private final SubscriptionManager subscriptionManager;
    private final IJsonSerialiser jsonSerialiser;
    private final Configurator configurator;
    private final ExecutionPlanRunner executionPlanRunner;
    private final DimensionMapUpdater dimensionMapUpdater;
    private SlaveDimensionMapper dimensionMapper;

    public InitialiseSlaveCommandHandler(CommandHandlerRegistry commandHandlerRegistry, IDataSourceRegistry dataSourceRegistry,
                                         SubscriptionManager subscriptionManager, IJsonSerialiser jsonSerialiser,
                                         Configurator configurator, ExecutionPlanRunner executionPlanRunner,
                                         DimensionMapUpdater dimensionMapUpdater,
                                         SlaveDimensionMapper dimensionMapper) {
        super(IInitialiseSlaveCommand.class);
        this.commandHandlerRegistry = commandHandlerRegistry;
        this.dataSourceRegistry = dataSourceRegistry;
        this.subscriptionManager = subscriptionManager;
        this.jsonSerialiser = jsonSerialiser;
        this.configurator = configurator;
        this.executionPlanRunner = executionPlanRunner;
        this.dimensionMapUpdater = dimensionMapUpdater;
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    protected void handleCommand(Command command, IInitialiseSlaveCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            log.info("Initialising slave node in {} mode", data.getType());

            DistributionManager distributionManager = new DistributionManager(data.getType(), dataSourceRegistry,
                    jsonSerialiser, peerSession.getExecutionContext(), null, null);

            commandHandlerRegistry.register("registerDataSource", new RegisterDataSourceCommandHandler(dataSourceRegistry, distributionManager, jsonSerialiser, dimensionMapUpdater, dimensionMapper));
            commandHandlerRegistry.register("updateDimensionMap", new UpdateDimensionMapCommandHandler(dimensionMapUpdater));
            commandHandlerRegistry.register("subscribe", new SubscribeHandler(subscriptionManager, distributionManager, configurator, executionPlanRunner));
            commandHandlerRegistry.register("tableEdit", new TableEditCommandHandler(dataSourceRegistry, null, null));
            commandHandlerRegistry.register("reset", new ResetCommandHandler(dataSourceRegistry, distributionManager, peerSession.getExecutionContext(), peerSession.getSystemCatalog(), dimensionMapper));

            IPeerSession masterSession = peerSession;
            peerSession.addDisconnectionHandler(new PeerSession.IDisconnectionHandler() {
                @Override
                public void handleDisconnect(IPeerSession peerSession) {
                    if (peerSession == masterSession) {
                        Resetter resetter = new Resetter(peerSession.getSystemCatalog(), dataSourceRegistry, distributionManager, peerSession.getExecutionContext(), dimensionMapper);
                        resetter.reset(new CommandResult(), true);
                    }
                }
            });

            commandResult.setSuccess(true).setComplete(true);
        } catch (Throwable ex) {
            log.error("Failed to initialise slave node", ex);
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
        }
    }
}
