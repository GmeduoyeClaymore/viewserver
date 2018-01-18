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

import io.viewserver.Constants;
import io.viewserver.catalog.CatalogHolder;
import io.viewserver.catalog.CatalogOutput;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.command.MultiCommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.calccol.ICalcColConfig;
import io.viewserver.operators.deserialiser.DeserialiserEventHandlerBase;
import io.viewserver.operators.deserialiser.DeserialiserOperator;
import io.viewserver.operators.group.GroupByOperator;
import io.viewserver.operators.group.IGroupByConfig;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.operators.projection.ProjectionOperator;
import io.viewserver.operators.union.UnionOperator;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

import java.util.*;

/**
 * Created by nick on 01/10/15.
 */
public class CoalescorOperator extends OperatorBase implements IConfigurableOperator<ICoalescorConfig>, ICatalog {
    private final CatalogHolder catalogHolder;
    private final CatalogOutput catalogOutput;
    private Input input;
    private Output output;
    private final IDistributionManager distributionManager;
    private final SummaryRegistry summaryRegistry;
    private boolean configured;
    private boolean distributed;

    public CoalescorOperator(String name, IExecutionContext executionContext, ICatalog catalog, IDistributionManager distributionManager,
                             SummaryRegistry summaryRegistry) {
        super(name, executionContext, catalog);
        this.distributionManager = distributionManager;
        this.summaryRegistry = summaryRegistry;

        input = new Input(Constants.IN, this);
        addInput(input);

        output = new Output(Constants.OUT, this);
        addOutput(output);

        catalogHolder = new CatalogHolder(this);
        catalogOutput = new CatalogOutput("catalog", this, catalogHolder);
        addOutput(catalogOutput);
    }

    @Override
    public void configure(ICoalescorConfig config, CommandResult configureResult) {
        try {
            List<ViewServerNode> nodes = distributionManager.getAggregatorNodes();
            boolean willDistribute = !nodes.isEmpty();

            if (willDistribute) {
                MultiCommandResult coalesceResult = MultiCommandResult.wrap("Coalescing", configureResult);
                CommandResult ensureCoalescingOperatorsResult = coalesceResult.getResultForDependency("Ensure coalescing operators");
                CommandResult plugInResult = coalesceResult.getResultForDependency("Plugging in");

                IOutput output = ensureCoalescingOperators(config, ensureCoalescingOperatorsResult, nodes);

                if (configured && !distributed && input.getProducer() != null) {
                    input.getProducer().unplug(input);
                }
                if (!configured || !distributed) {
                    output.plugIn(input);
                }

                plugInResult.setSuccess(true).setComplete(true);
            } else {
                configureResult.setSuccess(true).setComplete(true);
                if (config.getRemoteConfigurationResult() != null) {
                    config.getRemoteConfigurationResult().setSuccess(true).setComplete(true);
                }
            }

            distributed = willDistribute;
            configured = true;
        } catch (Throwable e) {
            configureResult.setSuccess(false).addMessage(e.getMessage()).setComplete(true);
            throw e;
        }
    }

    private IOutput ensureCoalescingOperators(ICoalescorConfig config, CommandResult configureResult, List<ViewServerNode> nodes) {
        MultiCommandResult coalesceResult = MultiCommandResult.wrap("Create coalescing operators", configureResult);
        boolean isResultDeferred = false;
        int nodeCount = nodes.size();

        String unionName = "union"; //String.format("%s_%s", getName(), "union");
        UnionOperator unionOperator = (UnionOperator) getOperator(unionName);
        if (unionOperator == null) {
            unionOperator = new UnionOperator(unionName, getExecutionContext(), this, new ChunkedColumnStorage(1024));
        }

        MultiCommandResult remoteConfigurationResult = null;
        CommandResult placeholderResult = null;
        if (config.getRemoteConfigurationResult() != null) {
            remoteConfigurationResult = MultiCommandResult.wrap("ensureCoalescingOperators", config.getRemoteConfigurationResult());
            placeholderResult = remoteConfigurationResult.getResultForDependency("Placeholder");
        }

        String target = config.getTarget();
        for (int i = 0; i < nodeCount; i++) {
            IPeerSession peerSession = nodes.get(i).getPeerSession();
            String deserialiserName = peerSession.getCatalogName(); //String.format("%s_%s", getName(), peerSession.getCatalogName());
            if (getOperator(deserialiserName) != null) {
                continue;
            }
            DeserialiserOperator deserialiser = new DeserialiserOperator(deserialiserName, getExecutionContext(),
                    this, peerSession, target, new ChunkedColumnStorage(1024));
            if (remoteConfigurationResult != null) {
                CommandResult deserialiserResult = remoteConfigurationResult.getResultForDependency(String.format("Coalescor %s - Deserialiser %s", getName(), deserialiserName));
                deserialiser.addEventHandler(new DeserialiserEventHandlerBase() {
                    @Override
                    public void onSnapshotComplete(DeserialiserOperator deserialiserOperator) {
                        deserialiserResult.setSuccess(true).setComplete(true);
                    }
                });
            }
            deserialiser.connect();
            deserialiser.getOutput().plugIn(unionOperator.getOrCreateInput(deserialiserName, peerSession.getConnectionId()));
        }
        if (placeholderResult != null) {
            placeholderResult.setSuccess(true).setComplete(true);
        }

        IOutput output = unionOperator.getOutput();

        if (config.isGroupBy()) {
            Map<String, CalcColOperator.CalculatedColumn> postSummaryCalculations = new HashMap<>();
            Map<String, IGroupByConfig.Summary> coalescingSummaries = new HashMap<>();
            coalescingSummaries.put("count", new IGroupByConfig.Summary("count", "sum", config.getCountColumnName()));
            List<IGroupByConfig.Summary> summaries = config.getSummaries();
            int summaryCount = summaries.size();
            for (int i = 0; i < summaryCount; i++) {
                IGroupByConfig.Summary summary = summaries.get(i);
                List<IGroupByConfig.Summary> summariesForCoalescor = DistributedSummaryHelper.getSummariesForCoalescor(summary, config.getCountColumnName());
                int summariesForCoalescorCount = summariesForCoalescor.size();
                for (int j = 0; j < summariesForCoalescorCount; j++) {
                    IGroupByConfig.Summary coalescingSummary = summariesForCoalescor.get(j);
                    coalescingSummaries.put(coalescingSummary.getName(), coalescingSummary);
                }
                List<CalcColOperator.CalculatedColumn> postSummaryCalcs = DistributedSummaryHelper.getPostSummaryCalculationsForCoalescor(summary);
                int postSummaryCalcsCount = postSummaryCalcs.size();
                for (int j = 0; j < postSummaryCalcsCount; j++) {
                    CalcColOperator.CalculatedColumn postSummaryCalculation = postSummaryCalcs.get(j);
                    postSummaryCalculations.put(postSummaryCalculation.getName(), postSummaryCalculation);
                }
            }

            String groupByName = "group"; //String.format("%s_%s", getName(), "group");
            GroupByOperator groupByOperator = (GroupByOperator) getOperator(groupByName);
            if (groupByOperator == null) {
                isResultDeferred = true;
                groupByOperator = new GroupByOperator(groupByName, getExecutionContext(), this,
                        summaryRegistry, new ChunkedColumnStorage(1024));
                CommandResult groupByResult = coalesceResult.getResultForDependency("Coalescor - configure groupby '" + groupByName + "'");
                groupByOperator.configure(new IGroupByConfig() {
                    @Override
                    public List<String> getGroupBy() {
                        return config.getGroupBy();
                    }

                    @Override
                    public List<Summary> getSummaries() {
                        return new ArrayList<>(coalescingSummaries.values());
                    }

                    @Override
                    public String getCountColumnName() {
                        return "_count";
                    }

                    @Override
                    public List<String> getSubtotals() {
                        return config.getSubtotals();
                    }

                }, groupByResult);
                output.plugIn(groupByOperator.getInput());
            }
            output = groupByOperator.getOutput();

            List<String> excludedColumns = new ArrayList<>();
            for (int i = 0; i < summaryCount; i++) {
                excludedColumns.addAll(DistributedSummaryHelper.getExcludeColumns(summaries.get(i)));
            }

            if (!postSummaryCalculations.isEmpty()) {
                String calcColName = "calc"; //String.format("%s_%s", getName(), "calc");
                CalcColOperator calcColOperator = (CalcColOperator) getOperator(calcColName);
                if (calcColOperator == null) {
                    isResultDeferred = true;
                    calcColOperator = new CalcColOperator(calcColName, getExecutionContext(), this,
                            new ChunkedColumnStorage(1024), getExecutionContext().getExpressionParser());
                    CommandResult calcColResult = coalesceResult.getResultForDependency("Coalescor - configure calccol '" + calcColName + "'");
                    calcColOperator.configure(new ICalcColConfig() {
                        @Override
                        public List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                            return new ArrayList<>(postSummaryCalculations.values());
                        }

                        @Override
                        public Map<String, String> getColumnAliases() {
                            return null;
                        }

                        @Override
                        public boolean isDataRefreshedOnColumnAdd() {
                            return false;
                        }
                    }, calcColResult);
                    output.plugIn(calcColOperator.getInput());
                }
                output = calcColOperator.getOutput();
            }

            if (!excludedColumns.isEmpty()) {
                String projectionName = "projection"; //String.format("%s_%s", getName(), "projection");
                ProjectionOperator projectionOperator = (ProjectionOperator) getOperator(projectionName);
                if (projectionOperator == null) {
                    isResultDeferred = true;
                    projectionOperator = new ProjectionOperator(projectionName, getExecutionContext(), this);
                    CommandResult projectionResult = coalesceResult.getResultForDependency("Coalescor - configure projection '" + projectionName + "'");
                    projectionOperator.configure(new IProjectionConfig() {
                        @Override
                        public ProjectionMode getMode() {
                            return ProjectionMode.Exclusionary;
                        }

                        @Override
                        public Collection<ProjectionColumn> getProjectionColumns() {
                            ArrayList<ProjectionColumn> projectionColumns = new ArrayList<>();
                            for (String excludedColumn : excludedColumns) {
                                projectionColumns.add(new ProjectionColumn(excludedColumn));
                            }
                            return projectionColumns;
                        }
                    }, projectionResult);
                    output.plugIn(projectionOperator.getInput());
                }
                output = projectionOperator.getOutput();
            }
        }

        if (!isResultDeferred) {
            configureResult.setSuccess(true).setComplete(true);
        }
        return output;
    }

    @Override
    public void doTearDown() {
        catalogHolder.tearDown();

        super.doTearDown();
    }

    @Override
    public IOutput getOutput() {
        return output;
    }

    @Override
    public ICatalog getParent() {
        return catalogHolder.getParent();
    }

    @Override
    public void registerOperator(IOperator operator) {
        catalogHolder.registerOperator(operator);
        catalogOutput.handleAdd(catalogHolder.getRowIdForOperator(operator));
    }

    @Override
    public IOperator getOperator(String name) {
        return catalogHolder.getOperator(name);
    }

    @Override
    public Collection<IOperator> getAllOperators() {
        return catalogHolder.getAllOperators();
    }

    @Override
    public void unregisterOperator(IOperator operator) {
        catalogOutput.handleRemove(catalogHolder.getRowIdForOperator(operator));
        catalogHolder.unregisterOperator(operator);
    }

    @Override
    public ICatalog createDescendant(String path) {
        return catalogHolder.createDescendant(path);
    }

    @Override
    public void addChild(ICatalog childCatalog) {
        catalogHolder.addChild(childCatalog);
    }

    @Override
    public void removeChild(ICatalog childCatalog) {
        catalogHolder.removeChild(childCatalog);
    }

    @Override
    public ICatalog getChild(String name) {
        return catalogHolder.getChild(name);
    }

    private class Input extends InputBase {
        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            ColumnHolder outHolder = output.getColumnHolderFactory().createColumnHolder(columnHolder.getName(), columnHolder);
            output.mapColumn(columnHolder, outHolder, getProducer().getCurrentChanges());
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            output.unmapColumn(columnHolder);
        }

        @Override
        protected void onRowAdd(int row) {
            output.handleAdd(row);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            output.handleUpdate(row);
        }

        @Override
        protected void onRowRemove(int row) {
            output.handleRemove(row);
        }

        @Override
        public void onTearDownRequested() {
            // coalescors should not be torn down when their producer is
        }
    }

    private class Output extends MappedOutputBase {
        public Output(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public IColumnHolderFactory getColumnHolderFactory() {
            return new PassThroughColumnHolderFactory();
        }

        @Override
        public ActiveRowTracker getRowTracker() {
            return input.getProducer().getRowTracker();
        }

        @Override
        public void clearData() {
            // don't clear the row tracker, as it's not ours
        }
    }
}
