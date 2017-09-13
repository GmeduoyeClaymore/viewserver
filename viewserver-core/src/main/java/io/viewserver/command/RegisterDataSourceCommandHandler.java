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
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.distribution.DistributionManager;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.messages.command.IRegisterDataSourceCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by nickc on 25/11/2014.
 */
public class RegisterDataSourceCommandHandler extends CommandHandlerBase<IRegisterDataSourceCommand> {
    private static final Logger log = LoggerFactory.getLogger(RegisterDataSourceCommandHandler.class);
    private static final String DATASOURCE_DEPENDENCY_PREFIX = "/" + IDataSourceRegistry.TABLE_NAME + "/";
    private IDataSourceRegistry<SlaveDataSource> dataSourceRegistry;
    private DistributionManager distributionManager;
    private IJsonSerialiser serialiser;
    private DimensionMapUpdater dimensionMapUpdater;
    private SlaveDimensionMapper dimensionMapper;

    public RegisterDataSourceCommandHandler(IDataSourceRegistry<SlaveDataSource> dataSourceRegistry,
                                            DistributionManager distributionManager,
                                            IJsonSerialiser serialiser,
                                            DimensionMapUpdater dimensionMapUpdater,
                                            SlaveDimensionMapper dimensionMapper) {
        super(IRegisterDataSourceCommand.class);
        this.dataSourceRegistry = dataSourceRegistry;
        this.distributionManager = distributionManager;
        this.serialiser = serialiser;
        this.dimensionMapUpdater = dimensionMapUpdater;
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    protected void handleCommand(Command command, IRegisterDataSourceCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            SlaveDataSource dataSource = serialiser.deserialise(data.getDataSource(), SlaveDataSource.class);
            dataSourceRegistry.register(dataSource);
            dataSource = dataSourceRegistry.get(dataSource.getName());

            // create the table immediately (which will also register dimensions)
            // TODO: this isn't the right approach, but it's simpler, for now
            final ICatalog dataSourceCatalog = IDataSourceRegistry.getDataSourceCatalog(dataSource,
                    peerSession.getSystemCatalog());
            Table table = new Table(dataSource.getName(), peerSession.getExecutionContext(),
                    dataSourceCatalog,
                    DataSourceHelper.getSchema(dataSource, dimensionMapper), new ChunkedColumnStorage(1024));
            table.setMetadata("rowIdMap", new TIntIntHashMap());
            table.initialise(1024);

            runDataSource(new DataSourceHolder(dataSource, data, peerSession, commandResult));
        } catch (Throwable e) {
            log.error("Failed to register data source", e);
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }

    private void runDataSource(DataSourceHolder dataSourceHolder) {
        if (!checkDependencies(dataSourceHolder)) {
            return;
        }

        final ICatalog dataSourceCatalog = IDataSourceRegistry.getDataSourceCatalog(dataSourceHolder.dataSource,
                dataSourceHolder.peerSession.getSystemCatalog());
        DataSourceHelper.runDataSourceExecutionPlan(dataSourceHolder.dataSource, dataSourceRegistry,
                dataSourceHolder.peerSession.getExecutionContext(),
                dataSourceCatalog,
                distributionManager,
                dataSourceHolder.commandResult);
        final ICommandResultListener listener = dataSourceHolder.commandResult.getListener();
        dataSourceHolder.commandResult.setListener(res -> {
            dataSourceRegistry.setStatus(dataSourceHolder.dataSource.getName(),
                    res.isSuccess() ? DataSourceStatus.INITIALIZED : DataSourceStatus.ERROR);
            if (listener != null) {
                listener.onResult(res);
            }
        });

        if (dataSourceHolder.data.hasDimensionMapUpdate()) {
            dimensionMapUpdater.update(dataSourceHolder.data.getDimensionMapUpdate());
        }
    }

    private boolean checkDependencies(DataSourceHolder dataSourceHolder) {
        final Set<String> dependencies = new HashSet<>();
        final List<IGraphNode> nodes = dataSourceHolder.dataSource.getNodes();
        final int nodeCount = nodes.size();
        for (int i = 0; i < nodeCount; i++) {
            final IGraphNode node = nodes.get(i);
            final List<IConfiguratorSpec.Connection> connections = node.getConnections();
            final int connectionCount = connections.size();
            for (int j = 0; j < connectionCount; j++) {
                final IConfiguratorSpec.Connection connection = connections.get(j);
                final String operator = connection.getOperator();
                if (operator.startsWith(DATASOURCE_DEPENDENCY_PREFIX)) {
                    final String suffix = operator.substring(DATASOURCE_DEPENDENCY_PREFIX.length());
                    final int slashIndex = suffix.indexOf("/");
                    if (slashIndex > -1) {
                        final String dependencyName = suffix.substring(0, slashIndex);
                        final DataSourceStatus status = dataSourceRegistry.getStatus(dependencyName);
                        if (status != DataSourceStatus.BUILT && status != DataSourceStatus.INITIALIZED) {
                            dependencies.add(dependencyName);
                        }
                    }
                }
            }
        }
        if (dependencies.isEmpty()) {
            log.debug("{} has no outstanding dependencies - will build", dataSourceHolder.dataSource.getName());
            return true;
        }

        dataSourceRegistry.addListener(new IDataSourceListener() {
            @Override
            public void onDataSourceRegistered(IDataSource dataSource) {
            }

            @Override
            public void onDataSourceStatusChanged(IDataSource dataSource, DataSourceStatus status) {
                if (dependencies.contains(dataSource.getName()) && (status == DataSourceStatus.BUILT
                        || status == DataSourceStatus.INITIALIZED)) {
                    dependencies.remove(dataSource.getName());
                    if (dependencies.isEmpty()) {
                        runDataSource(dataSourceHolder);
                    }
                }
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("{} has outstanding dependencies - {} - not building now", dataSourceHolder.dataSource.getName(),
                    StringUtils.join(dependencies, ','));
        }
        return false;
    }

    private class DataSourceHolder {
        SlaveDataSource dataSource;
        IRegisterDataSourceCommand data;
        IPeerSession peerSession;
        CommandResult commandResult;

        public DataSourceHolder(SlaveDataSource dataSource, IRegisterDataSourceCommand data, IPeerSession peerSession, CommandResult commandResult) {
            this.dataSource = dataSource;
            this.data = data;
            this.peerSession = peerSession;
            this.commandResult = commandResult;
        }
    }
}
