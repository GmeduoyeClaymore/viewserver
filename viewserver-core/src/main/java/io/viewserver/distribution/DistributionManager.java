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

package io.viewserver.distribution;

import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.*;
import io.viewserver.command.CommandResult;
import io.viewserver.command.ICommandResultListener;
import io.viewserver.command.MultiCommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.datasource.*;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IConfigurateCommand;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.messages.command.IRegisterDataSourceCommand;
import io.viewserver.messages.command.IUpdateDimensionMapCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.network.PeerSession;
import io.viewserver.reactor.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by nickc on 25/11/2014.
 */
public class DistributionManager implements IDistributionManager, PeerSession.IDisconnectionHandler, IDimensionMapperListener, IDataSourceListener {
    private static final Logger log = LoggerFactory.getLogger(DistributionManager.class);
    private final Map<IInitialiseSlaveCommand.Type, List<ViewServerNode>> nodesByType = new HashMap<>();
    private IDataSourceRegistry<? extends IDataSource> dataSourceRegistry;
    private IJsonSerialiser jsonSerialiser;
    private ExecutionContext executionContext;
    private DimensionMapper dimensionMapper;
    private ICatalog catalog;
    private IInitialiseSlaveCommand.Type nodeType;
    private Set<INodeMonitor> nodeMonitors = new HashSet<>();
    private final List<ViewServerNode> addedNodes = new ArrayList<>();
    private final ProcessNodeChangesTask processNodeChangesTask = new ProcessNodeChangesTask();
    private boolean processNodeChangesTaskScheduled;
    private final Map<IDataSource, Map<Dimension, List<MappedDimensionValue>>> pendingDimensionMappings = new HashMap<>();
    private int processNodeChangesDelay = 5000;
    private int minimumSlaves = 1;
    private final List<ViewServerNode> removedNodes = new ArrayList<>();

    public DistributionManager(IInitialiseSlaveCommand.Type nodeType,
                               IDataSourceRegistry dataSourceRegistry,
                               IJsonSerialiser jsonSerialiser,
                               ExecutionContext executionContext,
                               DimensionMapper dimensionMapper,
                               ICatalog catalog) {
        this.nodeType = nodeType;
        this.dataSourceRegistry = dataSourceRegistry;
        this.jsonSerialiser = jsonSerialiser;
        this.executionContext = executionContext;
        this.dimensionMapper = dimensionMapper;
        this.catalog = catalog;

        if (dimensionMapper != null) {
            dimensionMapper.setDimensionMapperListener(this);
        }

        dataSourceRegistry.addListener(this);
        executionContext.getReactor().addLoopTask(new DimensionMapUpdateTask(), 10);
    }

    @Override
    public IInitialiseSlaveCommand.Type getNodeType() {
        return nodeType;
    }

    public int getProcessNodeChangesDelay() {
        return processNodeChangesDelay;
    }

    @Override
    public void setProcessNodeChangesDelay(int processNodeChangesDelay) {
        this.processNodeChangesDelay = processNodeChangesDelay;
    }

    public int getMinimumSlaves() {
        return minimumSlaves;
    }

    @Override
    public void setMinimumSlaves(int minimumSlaves) {
        this.minimumSlaves = minimumSlaves;
    }

    @Override
    public void initialise() {
    }

    @Override
    public int getNumberOfSlaves() {
        int numberOfSlaves = 0;
        for (List<ViewServerNode> nodes : nodesByType.values()) {
            numberOfSlaves += nodes.size();
        }
        return numberOfSlaves;
    }

    @Override
    public void addNodeMonitor(INodeMonitor nodeMonitor, boolean catchUp) {
        nodeMonitors.add(nodeMonitor);

        if (catchUp) {
            for (List<ViewServerNode> viewServerNodes : nodesByType.values()) {
                for (ViewServerNode viewServerNode : viewServerNodes) {
                    nodeMonitor.onNodeAdded(viewServerNode);
                }
            }
            nodeMonitor.onNodesChanged(getAggregatorNodes(), Collections.emptyList(), new CommandResult());
        }
    }

    @Override
    public void removeNodeMonitor(INodeMonitor nodeMonitor) {
        nodeMonitors.remove(nodeMonitor);
    }

    @Override
    public List<ViewServerNode> getAggregatorNodes() {
        List<ViewServerNode> aggregatorNodes = nodesByType.get(IInitialiseSlaveCommand.Type.Aggregator);
        return aggregatorNodes != null ? Collections.unmodifiableList(aggregatorNodes) : new ArrayList<>();
    }

    @Override
    public void addNode(IPeerSession peerSession, CommandResult commandResult) {
        IInitialiseSlaveCommand.Type nodeType = getNodeType(peerSession);
        ViewServerNode node = new ViewServerNode(nodeType, peerSession);

        IInitialiseSlaveCommand initialiseSlaveCommandDto = MessagePool.getInstance().get(IInitialiseSlaveCommand.class)
                .setType(nodeType);
        Command initialiseSlaveCommand = new Command("initialiseSlave", initialiseSlaveCommandDto);
        initialiseSlaveCommand.setCommandResultListener(new ICommandResultListener() {
            @Override
            public void onResult(CommandResult initialiseSlaveResult) {
                if (initialiseSlaveResult.isSuccess()) {
                    node.setInitialised(true);
                    node.getPeerSession().addDisconnectionHandler(DistributionManager.this);
                    addedNodes.add(node);

                    ICatalog slavesCatalog = (ICatalog) catalog.getOperator("slaves");
                    if (slavesCatalog == null) {
                        slavesCatalog = new Catalog(SlaveCatalog.SLAVES_CATALOG_NAME, catalog);
                    }
                    new SlaveCatalog(peerSession, executionContext, slavesCatalog);

                    if (getNumberOfSlaves() + addedNodes.size() >= minimumSlaves) {
                        scheduleProcessNodeChangesTask();
                    } else {
                        log.info("Waiting for more slaves (minimum {} required", minimumSlaves);
                    }

                    if (commandResult != null) {
                        commandResult.setSuccess(true).setComplete(true);
                    }
                } else {
                    if (commandResult != null) {
                        commandResult.setSuccess(false).setMessage(initialiseSlaveResult.getMessage()).setComplete(true);
                    }
                }
            }
        });
        peerSession.sendCommand(initialiseSlaveCommand);
        initialiseSlaveCommandDto.release();
    }

    private void scheduleProcessNodeChangesTask() {
        if (!processNodeChangesTaskScheduled) {
            executionContext.getReactor().scheduleTask(processNodeChangesTask, processNodeChangesDelay, -1);
            processNodeChangesTaskScheduled = true;
            log.info("New nodes will be processed in {}ms", processNodeChangesDelay);
        }
    }

    private void initialiseDataSources(Iterable<ViewServerNode> nodes) {
        for (IDataSource dataSource : dataSourceRegistry.getAll()) {
            registerDataSource(dataSource, nodes);
        }
    }

    private void registerDataSource(IDataSource dataSource, Iterable<ViewServerNode> nodes) {
        if (nodes == null) {
            return;
        }

        if (dataSource.getDistributionMode() != DistributionMode.Local) {
            final String dataSourceJson = jsonSerialiser.serialise(SlaveDataSource.from(dataSource));
            if (dataSourceJson != null) {
                final IRegisterDataSourceCommand registerDataSourceCommandBuilder = MessagePool.getInstance().get(IRegisterDataSourceCommand.class)
                        .setDataSource(dataSourceJson);
                final IUpdateDimensionMapCommand dimensionMapUpdateBuilder = registerDataSourceCommandBuilder.getDimensionMapUpdate();
                final IUpdateDimensionMapCommand.IDataSource dataSourceBuilder = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IDataSource.class)
                        .setName(dataSource.getName());
                final List<IUpdateDimensionMapCommand.IDimension> dimensions = dataSourceBuilder.getDimensions();
                for (Dimension dimension : dataSource.getDimensions()) {
                    // special cases that don't require a map
                    if (dimension.getType() == ColumnType.Bool || dimension.getType() == ColumnType.NullableBool) {
                        continue;
                    }

                    HashPrimitiveIterator allValues = dimensionMapper.getAllValues(dataSource, dimension);
                    if (!allValues.hasNext()) {
                        continue;
                    }

                    final IUpdateDimensionMapCommand.IDimension dimensionBuilder = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IDimension.class)
                            .setName(dimension.getName());

                    final List<IUpdateDimensionMapCommand.IMapping> mappings = dimensionBuilder.getMappings();
                    switch (dimension.getType()) {
                        case Byte: {
                            while (allValues.hasNext()) {
                                byte value = ((ByteIterator) allValues).next();
                                final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                        .setId(allValues.getIndex())
                                        .setIntegerValue(value);
                                mappings.add(mapping);
                                mapping.release();
                            }
                            break;
                        }
                        case Short: {
                            while (allValues.hasNext()) {
                                short value = ((ShortIterator) allValues).next();
                                final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                        .setId(allValues.getIndex())
                                        .setIntegerValue(value);
                                mappings.add(mapping);
                                mapping.release();
                            }
                            break;
                        }
                        case Int: {
                            while (allValues.hasNext()) {
                                int value = ((IntIterator) allValues).next();
                                final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                        .setId(allValues.getIndex())
                                        .setIntegerValue(value);
                                mappings.add(mapping);
                                mapping.release();
                            }
                            break;
                        }
                        case Long: {
                            while (allValues.hasNext()) {
                                long value = ((LongIterator) allValues).next();
                                final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                        .setId(allValues.getIndex())
                                        .setLongValue(value);
                                mappings.add(mapping);
                                mapping.release();
                            }
                            break;
                        }
                        case String: {
                            while (allValues.hasNext()) {
                                String value = ((StringIterator) allValues).next();
                                final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                        .setId(allValues.getIndex());

                                if (value != null) {
                                    mapping.setStringValue(value);
                                } else {
                                    mapping.setNullValue();
                                }
                                mappings.add(mapping);
                                mapping.release();
                            }
                            break;
                        }
                        default: {
                            log.warn("Unsupported dimension type '{}' for dimension '{}' in data source '{}'",
                                    dimension.getType(), dimension.getName(), dataSource.getName());
                        }
                    }
                    dimensions.add(dimensionBuilder);
                    dimensionBuilder.release();
                }
                dimensionMapUpdateBuilder.getDataSources().add(dataSourceBuilder);
                dataSourceBuilder.release();

                for (ViewServerNode node : nodes) {
                    Command command = new Command("registerDataSource", registerDataSourceCommandBuilder);
                    node.getPeerSession().sendCommand(command);
                }
                registerDataSourceCommandBuilder.release();
            }
        }
    }

    private void fireNodeAdded(ViewServerNode node) {
        for (INodeMonitor nodeMonitor : nodeMonitors) {
            nodeMonitor.onNodeAdded(node);
        }
    }

    private void fireNodeRemoved(ViewServerNode node) {
        for (INodeMonitor nodeMonitor : nodeMonitors) {
            nodeMonitor.onNodeRemoved(node);
        }
    }

    private void fireNodesChanged(CommandResult commandResult) {
        List<ViewServerNode> addedNodes = Collections.unmodifiableList(this.addedNodes);
        List<ViewServerNode> removedNodes = Collections.unmodifiableList(this.removedNodes);
        MultiCommandResult monitorResults = MultiCommandResult.wrap("fireNodesChanged", commandResult);
        CommandResult placeholder = monitorResults.getResultForDependency("placeholder");
        for (INodeMonitor nodeMonitor : nodeMonitors) {
            CommandResult monitorResult = monitorResults.getResultForDependency(nodeMonitor.toString());
            nodeMonitor.onNodesChanged(addedNodes,
                    removedNodes,
                    monitorResult);
        }
        placeholder.setSuccess(true).setComplete(true);
    }

    @Override
    public void configureNodes(IConfigurateCommand configuratorSpec, CommandResult commandResult, List<ViewServerNode> nodesToConfigure) {
        List<ViewServerNode> nodes = nodesToConfigure != null ? nodesToConfigure : nodesByType.get(IInitialiseSlaveCommand.Type.Aggregator);
        if (!nodes.isEmpty()) {
            MultiCommandResult multiCommandResult = MultiCommandResult.wrap("DistributionManager.configureNodes", commandResult);
            int count = nodes.size();
            for (int i = 0; i < count; i++) {
                ViewServerNode node = nodes.get(i);
                CommandResult nodeResult = multiCommandResult.getResultForDependency("Node '" + node.toString() + "'");
                Command command = new Command("configurate", configuratorSpec);
                command.setCommandResultListener(new ICommandResultListener() {
                    @Override
                    public void onResult(CommandResult commandResult) {
                        nodeResult.setSuccess(commandResult.isSuccess())
                                .addMessage(commandResult.getMessage())
                                .setComplete(commandResult.isComplete());
                    }
                });
                node.getPeerSession().sendCommand(command);
            }
        } else {
            commandResult.setSuccess(true).setComplete(true);
        }
    }

    @Override
    public void resetAllNodes(CommandResult commandResult) {
        boolean waitingForNodes = false;
        MultiCommandResult multiNodeResult = MultiCommandResult.wrap("DistributionManager.resetAllNodes", commandResult);
        for (List<ViewServerNode> nodes : nodesByType.values()) {
            int count = nodes.size();
            for (int i = 0; i < count; i++) {
                ViewServerNode node = nodes.get(i);
                CommandResult nodeResult = multiNodeResult.getResultForDependency("Node '" + node.toString() + "'");
                Command resetCommand = new Command("reset");
                resetCommand.setCommandResultListener(new ICommandResultListener() {
                    @Override
                    public void onResult(CommandResult resetResult) {
                        nodeResult.setSuccess(resetResult.isSuccess())
                                .addMessage(resetResult.getMessage())
                                .setComplete(resetResult.isComplete());
                    }
                });
                node.getPeerSession().sendCommand(resetCommand);
                waitingForNodes = true;
            }
        }
        if (!waitingForNodes) {
            commandResult.setSuccess(true).setComplete(true);
        }
    }

    @Override
    public IInitialiseSlaveCommand.Type getNodeType(IPeerSession node) {
        return IInitialiseSlaveCommand.Type.Aggregator;
    }

    @Override
    public void handleDisconnect(IPeerSession peerSession) {
        ViewServerNode node = null;
        for (List<ViewServerNode> nodes : nodesByType.values()) {
            node = getNodeBySession(nodes, peerSession);
            if (node != null) {
                break;
            }
        }

        if (node != null) {
            removedNodes.add(node);
            scheduleProcessNodeChangesTask();
            return;
        }

        node = getNodeBySession(addedNodes, peerSession);
        if (node != null) {
            addedNodes.remove(node);
        }
    }

    private ViewServerNode getNodeBySession(Collection<ViewServerNode> nodes, IPeerSession peerSession) {
        for (ViewServerNode viewServerNode : nodes) {
            if (viewServerNode.getPeerSession() == peerSession) {
                return viewServerNode;
            }
        }
        return null;
    }

    @Override
    public void onDimensionValueMapped(IDataSource dataSource, Dimension dimension, int id, Object value) {
        if (nodesByType.isEmpty()) {
            return;
        }

        Map<Dimension, List<MappedDimensionValue>> dimensions = pendingDimensionMappings.get(dataSource);
        if (dimensions == null) {
            dimensions = new HashMap<>();
            pendingDimensionMappings.put(dataSource, dimensions);
        }
        List<MappedDimensionValue> mappedDimensionValues = dimensions.get(dimension);
        if (mappedDimensionValues == null) {
            mappedDimensionValues = new ArrayList<>();
            dimensions.put(dimension, mappedDimensionValues);
        }
        mappedDimensionValues.add(new MappedDimensionValue(id, value));
    }

//    @Override
//    public void onDataSourcesLoaded() {
//        initialiseDataSources(getAggregatorNodes());
//    }

    @Override
    public void onDataSourceRegistered(IDataSource dataSource) {
    }

    @Override
    public void onDataSourceStatusChanged(IDataSource dataSource, DataSourceStatus status) {
        if (status.equals(DataSourceStatus.BUILT)) {
            registerDataSource(dataSource, nodesByType.get(IInitialiseSlaveCommand.Type.Aggregator));
        }
    }

    private class ProcessNodeChangesTask implements ITask {
        @Override
        public void execute() {
            if (addedNodes.isEmpty() && removedNodes.isEmpty()) {
                processNodeChangesTaskScheduled = false;
                return;
            }

            executionContext.pause();
            MultiCommandResult commandResult = new MultiCommandResult(null);
            CommandResult placeholderResult = commandResult.getResultForDependency("placeholder");
            commandResult.setListener(res -> executionContext.resume());

            if (!removedNodes.isEmpty()) {
                log.info("{} slave nodes were removed", removedNodes.size());

                int count = removedNodes.size();
                for (int i = 0; i < count; i++) {
                    ViewServerNode node = removedNodes.get(i);
                    nodesByType.get(node.getType()).remove(node);
                    fireNodeRemoved(node);
                }
            }

            if (!addedNodes.isEmpty()) {
                log.info("{} new slave nodes were added", addedNodes.size());

                initialiseDataSources(addedNodes);

                int count = addedNodes.size();
                for (int i = 0; i < count; i++) {
                    ViewServerNode node = addedNodes.get(i);

                    List<ViewServerNode> nodes = nodesByType.get(node.getType());
                    if (nodes == null) {
                        nodesByType.put(node.getType(), (nodes = new ArrayList<>()));
                    }
                    nodes.add(node);

                    fireNodeAdded(node);
                }
            }

            CommandResult fireNodesChangedResult = commandResult.getResultForDependency("Fire nodes changed");
            fireNodesChanged(fireNodesChangedResult);
            addedNodes.clear();
            removedNodes.clear();

            placeholderResult.setSuccess(true).setComplete(true);
            processNodeChangesTaskScheduled = false;
        }
    }

    private class MappedDimensionValue {
        private final int id;
        private final Object value;

        public MappedDimensionValue(int id, Object value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MappedDimensionValue that = (MappedDimensionValue) o;

            if (id != that.id) return false;
            return !(value != null ? !value.equals(that.value) : that.value != null);

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    private class DimensionMapUpdateTask implements Runnable {
        @Override
        public void run() {
            if (pendingDimensionMappings.isEmpty()) {
                return;
            }

            final IUpdateDimensionMapCommand updateDimensionMapCommand = MessagePool.getInstance().get(IUpdateDimensionMapCommand.class);

            pendingDimensionMappings.entrySet().forEach(dataSource -> addDataSource(updateDimensionMapCommand, dataSource));

            List<ViewServerNode> aggregators = nodesByType.get(IInitialiseSlaveCommand.Type.Aggregator);
            int count = aggregators.size();
            for (int i = 0; i < count; i++) {
                Command command = new Command("updateDimensionMap", updateDimensionMapCommand);
                aggregators.get(i).getPeerSession().sendCommand(command);
            }

            updateDimensionMapCommand.release();

            pendingDimensionMappings.clear();
        }

        private void addDataSource(IUpdateDimensionMapCommand updateDimensionMapCommand, Map.Entry<IDataSource, Map<Dimension, List<MappedDimensionValue>>> dataSource) {
            if (dataSource.getValue().isEmpty()) {
                return;
            }

            final IUpdateDimensionMapCommand.IDataSource dataSourceMessage = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IDataSource.class)
                    .setName(dataSource.getKey().getName());
            dataSource.getValue().entrySet().forEach(dimensionValue -> addDimensionValue(dataSourceMessage, dimensionValue));
            updateDimensionMapCommand.getDataSources().add(dataSourceMessage);
            dataSourceMessage.release();
        }

        private void addDimensionValue(IUpdateDimensionMapCommand.IDataSource dataSourceMessage, Map.Entry<Dimension, List<MappedDimensionValue>> dimension) {
            if (dimension.getValue().isEmpty()) {
                return;
            }
            List<MappedDimensionValue> values = dimension.getValue();
            int valueCount = values.size();

            IUpdateDimensionMapCommand.IDimension dimensionBuilder = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IDimension.class)
                    .setName(dimension.getKey().getName());
            final List<IUpdateDimensionMapCommand.IMapping> mappings = dimensionBuilder.getMappings();
            switch (dimension.getKey().getType()) {
                case Byte: {
                    for (int i = 0; i < valueCount; i++) {
                        MappedDimensionValue mappedDimensionValue = values.get(i);
                        final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                .setId(mappedDimensionValue.id)
                                .setIntegerValue((byte) mappedDimensionValue.value);
                        mappings.add(mapping);
                        mapping.release();
                    }
                    break;
                }
                case Short: {
                    for (int i = 0; i < valueCount; i++) {
                        MappedDimensionValue mappedDimensionValue = values.get(i);
                        final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                .setId(mappedDimensionValue.id)
                                .setIntegerValue((short) mappedDimensionValue.value);
                        mappings.add(mapping);
                        mapping.release();
                    }
                    break;
                }
                case Int: {
                    for (int i = 0; i < valueCount; i++) {
                        MappedDimensionValue mappedDimensionValue = values.get(i);
                        final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                .setId(mappedDimensionValue.id)
                                .setIntegerValue((int) mappedDimensionValue.value);
                        mappings.add(mapping);
                        mapping.release();
                    }
                    break;
                }
                case Long: {
                    for (int i = 0; i < valueCount; i++) {
                        MappedDimensionValue mappedDimensionValue = values.get(i);
                        final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                .setId(mappedDimensionValue.id)
                                .setLongValue((long) mappedDimensionValue.value);
                        mappings.add(mapping);
                        mapping.release();
                    }
                    break;
                }
                case String: {
                    for (int i = 0; i < valueCount; i++) {
                        MappedDimensionValue mappedDimensionValue = values.get(i);
                        final IUpdateDimensionMapCommand.IMapping mapping = MessagePool.getInstance().get(IUpdateDimensionMapCommand.IMapping.class)
                                .setId(mappedDimensionValue.id);

                        if (mappedDimensionValue.value != null) {
                            mapping.setStringValue((String) mappedDimensionValue.value);
                        } else {
                            mapping.setNullValue();
                        }
                        mappings.add(mapping);
                        mapping.release();
                    }
                    break;
                }
            }
            dataSourceMessage.getDimensions().add(dimensionBuilder);
            dimensionBuilder.release();
        }
    }
}
