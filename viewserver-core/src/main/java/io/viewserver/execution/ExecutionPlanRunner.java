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

package io.viewserver.execution;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.command.MultiCommandResult;
import io.viewserver.configurator.ConfiguratorSpec;
import io.viewserver.configurator.IConfigurator;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.Hasher;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.distribution.DistributedSummaryHelper;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.nodes.*;
import io.viewserver.execution.plan.IExecutionPlan;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IConfigurateCommand;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.join.IColumnNameResolver;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.IGraphDefinition;
import io.viewserver.util.ViewServerException;

import java.util.*;
import java.util.function.Consumer;

public class ExecutionPlanRunner {
    private IConfigurator configurator;
    private IDistributionManager distributionManager;
    private final List<IProjectionConfig.ProjectionColumn> projectionColumns = new ArrayList<>();

    public ExecutionPlanRunner(IConfigurator configurator, IDistributionManager distributionManager) {
        this.configurator = configurator;
        this.distributionManager = distributionManager;
    }

    public ExecutionPlanRunner(IConfigurator configurator) {
        this.configurator = configurator;
    }

    public void setDistributionManager(IDistributionManager distributionManager) {
        this.distributionManager = distributionManager;
    }

    public <TContext extends IExecutionPlanContext> void executePlan(IExecutionPlan<TContext> executionPlan, TContext context,
                                                                     IExecutionContext executionContext, ICatalog catalog, CommandResult commandResult) {
        if (distributionManager == null) {
            throw new ViewServerException("distributionManager is null");
        }

        context.clearNodes();

        context.setExecutionContext(executionContext);
        context.setCatalog(catalog);

        List<IExecutionPlanStep<TContext>> steps = executionPlan.getSteps();
        int stepCount = steps.size();
        for (int i = 0; i < stepCount; i++) {
            steps.get(i).execute(context);
        }

        // non-aggregating queries are handled by the blotter node, which is the master node, so don't distribute
        boolean willDistribute = context.isDistributed();

        List<IGraphNode> graphNodes = context.getGraphNodes();
        if (graphNodes.isEmpty()) {
            commandResult.setSuccess(true).setComplete(true);
            return;
        }

        ParameterHelper parameterHelper = context.getParameterHelper();
        int nodeCount = graphNodes.size();
        Set<String> nodeNames = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) {
            nodeNames.add(graphNodes.get(i).getName());
            graphNodes.get(i).setParameterHelper(parameterHelper);
        }

        Map<String, String> hashedOperatorNames = context.getDefaultNodeNames();
        int defaultNodeCount = hashedOperatorNames.size();
        HashSet<IGraphNode> hashedNodes = new HashSet<>();
        boolean nodesWereHashed;
        do {
            nodesWereHashed = false;
            for (int i = 0; i < nodeCount; i++) {
                IGraphNode node = graphNodes.get(i);
                if (hashedNodes.contains(node)) {
                    continue;
                }

                List<IConfiguratorSpec.Connection> connections = node.getConnections();
                int connectionCount = connections.size();
                boolean allConnectionsOk = true;
                for (int j = 0; j < connectionCount; j++) {
                    IConfiguratorSpec.Connection connection = connections.get(j);
                    String inOperator = connection.getOperator();
                    // if it's a root operator, we don't hash the name
                    if (inOperator.charAt(0) == '/') {
                        continue;
                    }
                    // if it's not a part of this graph, don't hash it
                    if (!nodeNames.contains(inOperator)) {
                        continue;
                    }
                    // it's a graph node, so should be hashed
                    String hashedName = hashedOperatorNames.get(inOperator);
                    if (hashedName == null) {
                        allConnectionsOk = false;
                        break;
                    }
                }
                if (!allConnectionsOk) {
                    continue;
                }
                nodesWereHashed = true;
                hashedNodes.add(node);

                for (int j = 0; j < connectionCount; j++) {
                    IConfiguratorSpec.Connection connection = connections.get(j);
                    final String inOperator = connection.getOperator();
                    if (inOperator.charAt(0) == '/') {
                        connection.setOperator(inOperator);
                    } else if (nodeNames.contains(inOperator)) {
                        String hashedName = hashedOperatorNames.get(inOperator);
                        connection.setOperator(hashedName);
                    }
                }

                String operatorName = node.getNameForOperatorSpec(context.shouldHashNames());
                hashedOperatorNames.put(node.getName(), operatorName);
                context.setOperatorName(node.getName(), operatorName);
            }
        } while (nodesWereHashed);
        if (hashedOperatorNames.size() != nodeCount + defaultNodeCount) {
            throw new ViewServerException("Could not resolve all graph nodes - possible circular reference?");
        }

        MultiCommandResult coalescorResults = null;
        CommandResult coalescorPlaceholderResult = null;
        if (context.getRemoteConfigurationResult() != null) {
            coalescorResults = MultiCommandResult.wrap("Coalescors", context.getRemoteConfigurationResult());
            coalescorPlaceholderResult = coalescorResults.getResultForDependency("Placeholder");
        }

        addCoalescors(context, willDistribute, coalescorResults);
        if (willDistribute && context instanceof ReportExecutionPlanContext) {
            distributeAggregations((ReportExecutionPlanContext) context);
        }
        nodeCount = context.getGraphNodes().size();

        ConfiguratorSpec localConfiguratorSpec = new ConfiguratorSpec();
        IConfigurateCommand remoteConfiguratorSpec = MessagePool.getInstance().get(IConfigurateCommand.class);
        Map<String, IGraphNode> distributedNodes = new HashMap<>();
        for (int i = 0; i < nodeCount; i++) {
            IGraphNode node = graphNodes.get(i);
            if (willDistribute && node.isDistributed()) {
                IConfigurateCommand.IOperator operatorSpecDto = node.getOperatorSpecDto(parameterHelper);
                remoteConfiguratorSpec.getOperators().add(operatorSpecDto);
                distributedNodes.put(operatorSpecDto.getName(), node);

                // clean up any nodes in the master that were used when slaves weren't available to distribute to
                if (node.getName().charAt(0) != '/') {
                    localConfiguratorSpec.getOperators().add(new IConfiguratorSpec.OperatorSpec(
                            operatorSpecDto.getName(),
                            operatorSpecDto.getType(),
                            IConfiguratorSpec.OperatorSpec.Operation.Remove,
                            false,
                            null
                    ));
                }
            } else {
                IConfiguratorSpec.OperatorSpec operatorSpec = node.getOperatorSpec(parameterHelper, context.shouldHashNames());
                localConfiguratorSpec.getOperators().add(operatorSpec);
            }
        }

        MultiCommandResult multiCommandResult = MultiCommandResult.wrap("ExecutionPlanRunner", commandResult);

        String hashedInputName = hashedOperatorNames.get(context.getInputOperator());
        if (hashedInputName != null) {
            context.setInput(hashedInputName, context.getInputOutputName());
        }
        if (willDistribute) {
            IGraphNode distributedOutput = distributedNodes.get(context.getInputOperator());
            if (distributedOutput != null) {
                if (!(distributedOutput instanceof ICoalesceableGraphNode)) {
                    throw new ViewServerException(String.format("Cannot coalesce nodes of type '%s'", distributedOutput.getType()));
                }
                String coalescorName = distributedOutput.getName() + "_coalescor";
                CommandResult coalescorResult = coalescorResults != null ? coalescorResults.getResultForDependency(distributedOutput.getName()) : null;
                CoalescorNode coalescorNode = new CoalescorNode(coalescorName, (ICoalesceableGraphNode) distributedOutput, parameterHelper, coalescorResult);
                graphNodes.add(coalescorNode);
                IConfiguratorSpec.OperatorSpec coalescorOperatorSpec = coalescorNode.getOperatorSpec(parameterHelper, context.shouldHashNames());
                hashedOperatorNames.put(coalescorName, coalescorOperatorSpec.getName());
                localConfiguratorSpec.getOperators().add(coalescorOperatorSpec);
                context.setInput(coalescorOperatorSpec.getName());
            }

            if (distributionManager.getNodeType().equals(IInitialiseSlaveCommand.Type.Master)
                    && !remoteConfiguratorSpec.getOperators().isEmpty()) {
                CommandResult distributionResult = multiCommandResult.getResultForDependency("Distribution");
                distributionManager.configureNodes(remoteConfiguratorSpec, distributionResult, context.getViewServerNodes());
                remoteConfiguratorSpec.release();
            }
        }

        if (!localConfiguratorSpec.getOperators().isEmpty()) {
            CommandResult configuratorResult = multiCommandResult.getResultForDependency("Configurator");
            configurator.process(localConfiguratorSpec, executionContext, catalog, configuratorResult);
        }

        if (coalescorPlaceholderResult != null) {
            coalescorPlaceholderResult.setSuccess(true).setComplete(true);
        }
    }

    private <TContext extends IExecutionPlanContext> List<CoalescorNode> addCoalescors(TContext context, boolean willDistribute, MultiCommandResult coalescorResults) {
        ArrayList<CoalescorNode> coalescorNodes = new ArrayList<>();
        ArrayList<IGraphNode> nodes = new ArrayList<>(context.getGraphNodes());

        Map<String, IGraphNode> distributedNodes = new HashMap<>();
        int nodeCount = nodes.size();
        for (int i = 0; i < nodeCount; i++) {
            IGraphNode node = nodes.get(i);
            if (node.isDistributed()) {
                distributedNodes.put(node.getNameForOperatorSpec(true), node);
            }
        }

        Set<String> coalescors = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) {
            IGraphNode node = nodes.get(i);
            if (node.isDistributed()) {
                continue;
            }

            List<IConfiguratorSpec.Connection> connections = node.getConnections();
            int connectionCount = connections.size();
            for (int j = 0; j < connectionCount; j++) {
                IConfiguratorSpec.Connection connection = connections.get(j);
                IGraphNode targetNode = distributedNodes.get(connection.getOperator());
                if (targetNode != null) {
                    if (!(targetNode instanceof ICoalesceableGraphNode)) {
                        throw new ViewServerException(String.format("Cannot coalesce nodes of type '%s'", targetNode.getType()));
                    }
                    String coalescorName = targetNode.getName() + "_coalescor";
                    if (!coalescors.contains(coalescorName)) {
                        CommandResult coalescorResult = coalescorResults != null ? coalescorResults.getResultForDependency(targetNode.getName()) : null;
                        CoalescorNode coalescorNode = new CoalescorNode(coalescorName, (ICoalesceableGraphNode) targetNode, context.getParameterHelper(), coalescorResult);
                        if (!willDistribute) {
                            coalescorNode.withConnection(connection.getOperator(), connection.getOutput(), connection.getInput());
                        }
                        final String coalescorOperatorName = coalescorNode.getNameForOperatorSpec(true);
                        connection.setOperator(coalescorOperatorName);
                        context.addNodes(coalescorNode);
                        context.setOperatorName(coalescorName, coalescorOperatorName);
                        coalescors.add(coalescorName);
                        coalescorNodes.add(coalescorNode);
                    }
                }
            }
        }

        if (context instanceof ReportExecutionPlanContext) {
            IGraphDefinition graphDefinition = ((ReportExecutionPlanContext) context).getGraphDefinition();
            if (graphDefinition != null) {
                String output = graphDefinition.getOutput();
                for (int i = 0; i < nodeCount; i++) {
                    IGraphNode node = nodes.get(i);
                    if (node.getName().equals(output) && node.isDistributed()) {
                        if (!(node instanceof ICoalesceableGraphNode)) {
                            throw new ViewServerException(String.format("Cannot coalesce nodes of type '%s'", node.getType()));
                        }
                        String coalescorName = node.getName() + "_coalescor";
                        if (!coalescors.contains(coalescorName)) {
                            CommandResult coalescorResult = coalescorResults != null ? coalescorResults.getResultForDependency(node.getName()) : null;
                            CoalescorNode coalescorNode = new CoalescorNode(coalescorName, (ICoalesceableGraphNode) node, context.getParameterHelper(), coalescorResult);
                            if (!willDistribute) {
                                coalescorNode.withConnection(node.getNameForOperatorSpec(true));
                            }
                            final String coalescorOperatorName = coalescorNode.getNameForOperatorSpec(true);
                            graphDefinition.setOutput(coalescorOperatorName);
                            context.addNodes(coalescorNode);
                            context.setOperatorName(coalescorName, coalescorOperatorName);
                            coalescors.add(coalescorName);
                            coalescorNodes.add(coalescorNode);
                        }
                        break;
                    }
                }
            }
        }

        return coalescorNodes;
    }

    private void distributeAggregations(ReportExecutionPlanContext context) {
        List<CalcColOperator.CalculatedColumn> preSummaryCalculations = new ArrayList<>();
        List<IGraphNode> nodes = context.getGraphNodes();
        int nodeCount = nodes.size();
        for (int i = 0; i < nodeCount; i++) {
            IGraphNode node = nodes.get(i);
            if (node instanceof GroupByNode && node.isDistributed()) {
                Map<String, GroupByNode.SummaryDefinition> newSummaries = new HashMap<>();
                List<GroupByNode.SummaryDefinition> summaries = ((GroupByNode) node).getSummaries();
                int summaryCount = summaries.size();
                for (int j = 0; j < summaryCount; j++) {
                    GroupByNode.SummaryDefinition summaryDefinition = summaries.get(j);
                    // add pre-summary calculations to the global calc col node
                    List<CalcColOperator.CalculatedColumn> calculations = DistributedSummaryHelper.getPreSummaryCalculationsForPartition(summaryDefinition, context.getParameterHelper());
                    if (calculations != null) {
                        int calculationCount = calculations.size();
                        for (int k = 0; k < calculationCount; k++) {
                            CalcColOperator.CalculatedColumn calculation = calculations.get(k);
                            String columnName = "calc_" + Hasher.SHA1(calculation.getExpression());
                            if (context.getCalculationAliases().put(calculation.getName(), columnName) == null) {
                                calculation.setName(columnName);
                                preSummaryCalculations.add(calculation);
                            }
                        }
                    }

                    // transform the summary for partitioning
                    List<GroupByNode.SummaryDefinition> summariesForPartition = DistributedSummaryHelper.getSummariesForPartition(summaryDefinition, context.getParameterHelper());
                    int summariesForPartitionCount = summariesForPartition.size();
                    for (int k = 0; k < summariesForPartitionCount; k++) {
                        GroupByNode.SummaryDefinition partitionSummary = summariesForPartition.get(k);
                        newSummaries.put(partitionSummary.getName(), partitionSummary);
                    }
                }
                ((GroupByNode) node).setSummaries(new ArrayList<>(newSummaries.values()));
            }
        }

        if (!preSummaryCalculations.isEmpty()) {
            CalcColNode globalCalcColNode = null;
            ProjectionNode calcProjectionNode = null;
            String globalCalcColName = IDataSourceRegistry.getOperatorPath(context.getDataSource(), DataSource.CALCS_NAME);
            for (int i = 0; i < nodeCount; i++) {
                IGraphNode graphNode = nodes.get(i);
                if (graphNode.getName().equals(globalCalcColName)) {
                    globalCalcColNode = (CalcColNode) graphNode;
                } else if (graphNode.getName().equals("__projection")) {
                    calcProjectionNode = (ProjectionNode) graphNode;
                }
            }
            if (globalCalcColNode == null) {
                globalCalcColNode = new CalcColNode(globalCalcColName)
                        .withColumnAliases(context.getCalculationAliases())
                        .withDataRefreshedOnColumnAdd(false)
                        .withDistribution();
                context.addNodes(globalCalcColNode);
            }
            globalCalcColNode.withCalculations(preSummaryCalculations);

            projectionColumns.clear();
            context.getCalculationAliases().entrySet().forEach(addProjectionColumnProc);

            if (calcProjectionNode == null) {
                calcProjectionNode = new ProjectionNode("__projection")
                        .withMode(IProjectionConfig.ProjectionMode.Projection)
                        .withConnection(globalCalcColName, Constants.OUT, Constants.IN)
                        .withDistribution();
                context.addNodes(calcProjectionNode);
            }
            calcProjectionNode.withProjectionColumns(projectionColumns);
        }
    }

    private final Consumer<Map.Entry<String, String>> addProjectionColumnProc = entry -> {
        projectionColumns.add(new IProjectionConfig.ProjectionColumn(entry.getValue(), entry.getKey()));
    };
}
