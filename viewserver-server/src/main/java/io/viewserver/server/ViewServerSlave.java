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

package io.viewserver.server;

import io.viewserver.command.InitialiseSlaveCommandHandler;
import io.viewserver.datasource.DimensionMapUpdater;
import io.viewserver.datasource.SlaveDataSource;
import io.viewserver.datasource.SlaveDataSourceRegistry;
import io.viewserver.datasource.SlaveDimensionMapper;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IAuthenticateCommand;
import io.viewserver.messages.command.IRegisterSlaveCommand;
import io.viewserver.network.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.reactor.EventLoopReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ViewServerSlave extends ViewServerBase<SlaveDataSource> implements PeerSession.IDisconnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(ViewServerSlave.class);
    private IEndpoint masterEndpoint;
    private final SlaveDimensionMapper dimensionMapper = new SlaveDimensionMapper();
    private DimensionMapUpdater dimensionMapUpdater;
    private IPeerSession masterSession;

    public ViewServerSlave(String name, IEndpoint masterEndpoint) {
        super(name);
        this.masterEndpoint = masterEndpoint;
        log.info(String.format("Creating ViewServerSlave %s connected to %s", name, masterEndpoint));
    }

    @Override
    protected void initialise() {
        dataSourceRegistry = new SlaveDataSourceRegistry(getServerCatalog(), getServerExecutionContext(), jsonSerialiser);
        executionPlanRunner = new ExecutionPlanRunner(configurator, null);
        dimensionMapUpdater = new DimensionMapUpdater(dimensionMapper, getDataSourceRegistry());
        getServerExecutionContext().getExpressionParser().setDimensionMapper(dimensionMapper);

        super.initialise();

        connectToMaster();
    }

    private void connectToMaster() {
        ListenableFuture<IPeerSession> connectFuture = getServerNetwork().connect(masterEndpoint);
        getServerReactor().addCallback(connectFuture, new FutureCallback<IPeerSession>() {
            @Override
            public void onSuccess(IPeerSession peerSession) {
                masterSession = peerSession;
                masterSession.addDisconnectionHandler(ViewServerSlave.this);
                authenticate();
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("Fatal error happened during startup", t);
            }
        });
    }

    private void authenticate() {
        IAuthenticateCommand authenticateCommandDto = MessagePool.getInstance().get(IAuthenticateCommand.class)
                .setType("slave");
        authenticateCommandDto.getTokens().add(UUID.randomUUID().toString());
        Command authenticateCommand = new Command("authenticate", authenticateCommandDto);
        authenticateCommand.setCommandResultListener(commandResult -> {
            if (commandResult.isSuccess()) {
                register();
            }
        });
        masterSession.sendCommand(authenticateCommand);
        authenticateCommandDto.release();
    }

    private void register() {
        final IRegisterSlaveCommand registerSlaveCommand = MessagePool.getInstance().get(IRegisterSlaveCommand.class);
        Command command = new Command("registerSlave", registerSlaveCommand);
        masterSession.sendCommand(command);
        registerSlaveCommand.release();
    }

    @Override
    protected void initCommandHandlerRegistry() {
        super.initCommandHandlerRegistry();

        // commands we can receive
        commandHandlerRegistry.register("initialiseSlave",
                new InitialiseSlaveCommandHandler(commandHandlerRegistry, getDataSourceRegistry(),
                        getSubscriptionManager(), jsonSerialiser, configurator, executionPlanRunner, dimensionMapUpdater, dimensionMapper));
    }

    @Override
    protected EventLoopReactor getReactor(Network serverNetwork) {
        return new EventLoopReactor(super.getName(), serverNetwork);
    }

    @Override
    public void handleDisconnect(IPeerSession peerSession) {
        if (peerSession != masterSession) {
            return;
        }

        masterSession = null;

        getServerReactor().scheduleTask(this::connectToMaster, 1000, -1);
    }
}
