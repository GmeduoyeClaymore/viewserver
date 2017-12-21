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

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.command.MultiCommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.distribution.INodeMonitor;
import io.viewserver.distribution.ViewServerNode;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.SystemReportExecutor;

import java.util.List;

/**
 * Created by nick on 10/11/15.
 */
public class ReportDistributor implements INodeMonitor {
    private final IExecutionContext executionContext;
    private final ICatalog catalog;
    private final ReportContextRegistry reportContextRegistry;
    private final SystemReportExecutor systemReportExecutor;
    private int nodeCount;

    public ReportDistributor(IExecutionContext executionContext, ICatalog catalog, ReportContextRegistry reportContextRegistry,
                             SystemReportExecutor systemReportExecutor) {
        this.executionContext = executionContext;
        this.catalog = catalog.getChild("graphNodes");
        this.reportContextRegistry = reportContextRegistry;
        this.systemReportExecutor = systemReportExecutor;
    }

    @Override
    public void onNodeAdded(ViewServerNode node) {
    }

    @Override
    public void onNodeRemoved(ViewServerNode node) {
    }

    @Override
    public void onNodesChanged(List<ViewServerNode> addedNodes, List<ViewServerNode> removedNodes, CommandResult commandResult) {
        nodeCount += addedNodes.size() - removedNodes.size();

        // if we're only removing nodes, and there are still other nodes live, we don't need to do anything
        if (addedNodes.isEmpty() && nodeCount > 0) {
            commandResult.setSuccess(true).setComplete(true);
            return;
        }

        List<ReportContext> reportContexts = reportContextRegistry.getAllContexts();
        int count = reportContexts.size();
        MultiCommandResult updateExistingReportsResult = MultiCommandResult.wrap("updateExistingReports-remote", commandResult);
        CommandResult placeholderResult = updateExistingReportsResult.getResultForDependency("placeholder");
        MultiCommandResult contextsResult = new MultiCommandResult("updateExistingReports-context", null);
        for (int i = 0; i < count; i++) {
            ReportContext reportContext = reportContexts.get(i);
            String dependencyName = String.format("ReportContext-%d", i);
            CommandResult contextResult = contextsResult.getResultForDependency(dependencyName);
            CommandResult remoteConfigurationDependencyResult = updateExistingReportsResult.getResultForDependency(dependencyName);
            systemReportExecutor.executeContext(reportContext, executionContext, catalog, contextResult, "", remoteConfigurationDependencyResult, addedNodes);
        }
        placeholderResult.setSuccess(true).setComplete(true);
    }
}
