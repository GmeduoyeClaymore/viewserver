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

package io.viewserver.report;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.IntHashSet;
import io.viewserver.command.CommandResult;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.distribution.INodeMonitor;
import io.viewserver.distribution.SlaveCatalog;
import io.viewserver.distribution.ViewServerNode;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.InputOperatorBase;
import io.viewserver.operators.OutputBase;
import io.viewserver.operators.table.TableRow;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolderUtils;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.viewserver.schema.column.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by nick on 31/03/2015.
 */
public class ReportCatalog extends InputOperatorBase implements INodeMonitor {
    public static final String NAME_COLUMN = "name";
    public static final String TYPE_COLUMN = "type";
    public static final String DISTRIBUTED_COLUMN = "distributed";
    public static final String OPNAME_COLUMN = "opName";
    public static final String PATH_COLUMN = "path";
    private static final Logger log = LoggerFactory.getLogger(ReportCatalog.class);
    private final Output output;
    private final TIntObjectHashMap<NodeSpec> nodes = new TIntObjectHashMap<>();
    private final IntHashSet nodeIds = new IntHashSet(8, 0.75f, -1);
    private ReportContextExecutionPlanContext executionPlanContext;
    private IDistributionManager distributionManager;
    private ITableStorage storage;
    protected TableRow tableRow;
    private boolean initialised;

    public ReportCatalog(String name, ReportContextExecutionPlanContext executionPlanContext, ICatalog parent,
                         IDistributionManager distributionManager, ITableStorage storage) {
        super(name, parent.getExecutionContext(), parent);
        this.executionPlanContext = executionPlanContext;
        this.distributionManager = distributionManager;
        this.storage = storage;

        output = new Output(Constants.OUT, this);
        addOutput(output);
        tableRow = new TableRow(0, output.getSchema());

        initialise(1024);

        setSystemOperator(true);

        registerNodes();

        distributionManager.addNodeMonitor(this, false);


    }

    public void initialise(int capacity) {
        if (initialised) {
            throw new RuntimeException("Table already initialised");
        }

        storage.initialise(capacity, output.getSchema(), output.getCurrentChanges());

        initialised = true;
    }


    private void registerNodes() {
        List<ViewServerNode> viewServerNodes = distributionManager.getAggregatorNodes();
        List<IGraphNode> nodes = executionPlanContext.getGraphNodes();
        int count = nodes.size();
        for (int i = 0; i < count; i++) {
            registerNode(nodes.get(i), viewServerNodes);
        }
    }

    private void registerNode(IGraphNode node, List<ViewServerNode> viewServerNodes) {
        if (node.isDistributed() && !viewServerNodes.isEmpty()) {
            int count = viewServerNodes.size();
            for (int i = 0; i < count; i++) {
                registerNode(new NodeSpec(node, viewServerNodes.get(i)));
            }
        } else {
            registerNode(new NodeSpec(node));
        }
    }

    private void registerNode(NodeSpec nodeSpec) {
        int hash = nodeSpec.hashCode();
        nodes.put(hash, nodeSpec);
        int rowId = nodeIds.addInt(hash);
        tableRow.setRowId(rowId);
        tableRow.setString(PATH_COLUMN, nodeSpec.path);
        tableRow.setString(OPNAME_COLUMN, nodeSpec.getOpName());
        tableRow.setBool(DISTRIBUTED_COLUMN, nodeSpec.isDistributed());
        tableRow.setString(TYPE_COLUMN, nodeSpec.getType());
        tableRow.setString(NAME_COLUMN, nodeSpec.getName());
        output.handleAdd(rowId);
    }


    @Override
    public void onNodeAdded(ViewServerNode node) {
        // first node
        if (distributionManager.getAggregatorNodes().size() == 1) {
            nodes.forEachEntry((id, spec) -> {
                if (spec.isDistributed() && spec.getSlave() == null) {
                    nodes.remove(id);
                    int hash = spec.hashCode();
                    int rowId = nodeIds.index(hash);
                    nodeIds.remove(hash);
                    output.handleRemove(rowId);
                }
                return true;
            });
        }

        List<IGraphNode> graphNodes = executionPlanContext.getGraphNodes();
        int count = graphNodes.size();
        for (int i = 0; i < count; i++) {
            IGraphNode graphNode = graphNodes.get(i);
            if (graphNode.isDistributed()) {
                registerNode(new NodeSpec(graphNode, node));
            }
        }
    }

    @Override
    public void onNodeRemoved(ViewServerNode node) {
        nodes.forEachEntry((id, spec) -> {
            if (spec.isDistributed() && spec.getSlave().equals(node)) {
                nodes.remove(id);
                int hash = spec.hashCode();
                int rowId = nodeIds.index(hash);
                nodeIds.remove(hash);
                output.handleRemove(rowId);
            }
            return true;
        });

        // last node
        if (distributionManager.getAggregatorNodes().isEmpty()) {
            List<IGraphNode> graphNodes = executionPlanContext.getGraphNodes();
            int count = graphNodes.size();
            for (int i = 0; i < count; i++) {
                IGraphNode graphNode = graphNodes.get(i);
                if (graphNode.isDistributed()) {
                    registerNode(new NodeSpec(graphNode));
                }
            }
        }
    }

    @Override
    public void onNodesChanged(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes, CommandResult commandResult) {
        commandResult.setSuccess(true).setComplete(true);
    }

    private class Output extends OutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);

            Schema schema = getSchema();
            schema.addColumn(ColumnHolderUtils.createColumnHolder(NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(TYPE_COLUMN, io.viewserver.schema.column.ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(DISTRIBUTED_COLUMN, ColumnType.Bool));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(OPNAME_COLUMN, ColumnType.String));
            schema.addColumn(ColumnHolderUtils.createColumnHolder(PATH_COLUMN, ColumnType.String));
        }
    }

    private class NodeSpec {
        private ViewServerNode slave;
        private String path;
        private IGraphNode node;
        private String name;

        public NodeSpec(IGraphNode node) {
            this(node, null);
        }

        public NodeSpec(IGraphNode node, ViewServerNode slave) {
            this.slave = slave;
            this.node = node;

            name = getOpName();
            if (node.isDistributed() && slave != null) {
                path = String.format("/%s/%s/%s",
                        SlaveCatalog.SLAVES_CATALOG_NAME,
                        slave.getPeerSession().getCatalogName(),
                        name.charAt(0) == '/' ? name.substring(1) : name);
            }
        }

        public String getName() {
            return node.getName();
        }

        public String getType() {
            return node.getType();
        }

        public String getPath() {
            if (node.isDistributed() && slave != null) {
                return path;
            } else {
                IOperator operator = getCatalog().getOperator(name.charAt(0) == '/' ? name : String.format("/graphNodes/%s", name));
                if (operator != null) {
                    return operator.getPath();
                } else {
                    log.warn("Could not find operator for graph node '{}'", getName());
                    return "?";
                }
            }
        }

        public String getOpName() {
            return executionPlanContext.getOperatorName(node.getName());
        }

        public ViewServerNode getSlave() {
            return slave;
        }

        public boolean isDistributed() {
            return node.isDistributed();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NodeSpec nodeSpec = (NodeSpec) o;

            if (slave != null ? !slave.equals(nodeSpec.slave) : nodeSpec.slave != null) return false;
            return node.equals(nodeSpec.node);

        }

        @Override
        public int hashCode() {
            int result = slave != null ? slave.hashCode() : 0;
            result = 31 * result + node.hashCode();
            return result;
        }
    }
}
