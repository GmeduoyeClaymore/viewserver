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

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.command.UpdateSubscriptionHandler;
import io.viewserver.configurator.ConfiguratorSpec;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.IExecutionContext;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.plan.IExecutionPlan;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.util.ViewServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ExecutionPlanRunner implements IExecutionPlanRunner {
    private static final Logger log = LoggerFactory.getLogger(UpdateSubscriptionHandler.class);


    @Override
    public <TContext extends IExecutionPlanContext> void executePlan(IExecutionPlan<TContext> executionPlan, TContext context,
                                                                     IExecutionContext executionContext, ICatalog catalog, CommandResult commandResult) {

        log.debug("Commencing execution plan");
        context.clearNodes();

        context.setExecutionContext(executionContext);
        context.setCatalog(catalog);

        List<IExecutionPlanStep<TContext>> steps = executionPlan.getSteps();
        int stepCount = steps.size();
        log.debug("Found {} steps in execution plan",stepCount);
        for (int i = 0; i < stepCount; i++) {
            log.debug("Executing step {}",steps.get(i));
            steps.get(i).execute(context);
        }


        List<IGraphNode> graphNodes = context.getGraphNodes();
        if (graphNodes.isEmpty()) {
            log.debug("Setting success no nodes to created");
            commandResult.setSuccess(true).setComplete(true);
            return;
        }

        log.debug("Found {} graph nodes to create", graphNodes.size());
        ParameterHelper parameterHelper = context.getParameterHelper();
        int nodeCount = graphNodes.size();
        Set<String> nodeNames = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) {
            log.debug("Found  graph node {}", graphNodes.get(i).getName());
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
                    log.debug("node {} already created not creating again", node);
                    continue;
                }

                List<IConfiguratorSpec.Connection> connections = node.getConnections();
                int connectionCount = connections.size();
                boolean allConnectionsOk = true;
                log.debug("Creating {} connections for {}", connections.size(),node.getName());
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
                    log.debug("All connections for {} are not ok aborting",node.getName());
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
                    log.debug("Found connection {}",connection);
                }

                String operatorName = node.getNameForOperatorSpec(context.shouldHashNames());
                log.debug("Adding hashed operator name {} for {}",node.getName(), operatorName);
                hashedOperatorNames.put(node.getName(), operatorName);
                context.setOperatorName(node.getName(), operatorName);
            }
        } while (nodesWereHashed);
        if (hashedOperatorNames.size() != nodeCount + defaultNodeCount) {
            throw new ViewServerException("Could not resolve all graph nodes - possible circular reference?");
        }


        nodeCount = context.getGraphNodes().size();

        ConfiguratorSpec localConfiguratorSpec = new ConfiguratorSpec();

        for (int i = 0; i < nodeCount; i++) {
            IGraphNode node = graphNodes.get(i);
            IConfiguratorSpec.OperatorSpec operatorSpec = node.getOperatorSpec(parameterHelper, context.shouldHashNames());
            localConfiguratorSpec.getOperators().add(operatorSpec);
        }


        String hashedInputName = hashedOperatorNames.get(context.getInputOperator());
        if (hashedInputName != null) {
            context.setInput(hashedInputName, context.getInputOutputName());
        }

    }

}
