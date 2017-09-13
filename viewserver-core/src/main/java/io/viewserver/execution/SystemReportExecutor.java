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
import io.viewserver.command.MultiCommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.datasource.DataSourceStatus;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.distribution.ViewServerNode;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.plan.IExecutionPlan;
import io.viewserver.execution.plan.IMultiContextHandler;
import io.viewserver.execution.plan.MultiContextHandlerRegistry;
import io.viewserver.execution.plan.SystemReportExecutionPlan;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.util.ViewServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 05/10/15.
 */
public class SystemReportExecutor {
    private final MultiContextHandlerRegistry multiContextHandlerRegistry;
    private final DimensionMapper dimensionMapper;
    private final ExecutionPlanRunner executionPlanRunner;
    private final IDistributionManager distributionManager;
    private final IDataSourceRegistry dataSourceRegistry;
    private final ReportRegistry reportRegistry;

    public SystemReportExecutor(MultiContextHandlerRegistry multiContextHandlerRegistry,
                                DimensionMapper dimensionMapper,
                                ExecutionPlanRunner executionPlanRunner,
                                IDistributionManager distributionManager,
                                IDataSourceRegistry dataSourceRegistry,
                                ReportRegistry reportRegistry) {
        this.multiContextHandlerRegistry = multiContextHandlerRegistry;
        this.dimensionMapper = dimensionMapper;
        this.executionPlanRunner = executionPlanRunner;
        this.distributionManager = distributionManager;
        this.dataSourceRegistry = dataSourceRegistry;
        this.reportRegistry = reportRegistry;
    }

    public ReportContextExecutionPlanContext executeContext(ReportContext reportContext, ExecutionContext executionContext, ICatalog systemCatalog, CommandResult commandResult, String prefix, CommandResult remoteConfigurationResult, List<ViewServerNode> viewServerNodes) {
        IExecutionPlan activeExecutionPlan;
        ReportContextExecutionPlanContext activeExecutionPlanContext;

        ReportDefinition reportDefinition = getReportDefinition(reportContext);
        IDataSource dataSource = getDataSource(reportDefinition.getDataSource());

        if (!reportContext.getChildContexts().isEmpty()) {
            MultiCommandResult childContextsResult = MultiCommandResult.wrap("ExecuteContext" + prefix, commandResult);
            CommandResult childContextsPlaceholder = childContextsResult.getResultForDependency("Placeholder");
            MultiCommandResult childContextRemoteConfigurationResults = MultiCommandResult.wrap("ExecuteContext" + prefix, remoteConfigurationResult);
            CommandResult childContextRemoteConfigurationPlaceholder = childContextRemoteConfigurationResults.getResultForDependency("Placeholder");
            List<IExecutionPlanContext> childExecutionPlanContexts = new ArrayList<>();
            for (int i = 0; i < reportContext.getChildContexts().size(); i++) {
                ReportContext context = reportContext.getChildContexts().get(i);
                String childPrefix = prefix + ("".equals(prefix) ? "" : ".") + i;
                CommandResult childContextResult = childContextsResult.getResultForDependency("Child context " + childPrefix);
                CommandResult childContextRemoteConfigurationResult = childContextRemoteConfigurationResults.getResultForDependency("Child context " + childPrefix);
                IExecutionPlanContext childExecutionPlanContext = executeContext(context,
                        executionContext,
                        systemCatalog,
                        childContextResult,
                        childPrefix,
                        childContextRemoteConfigurationResult,
                        viewServerNodes);
                childExecutionPlanContexts.add(childExecutionPlanContext);
            }

            IMultiContextHandler multiContextHandler = multiContextHandlerRegistry.get(reportContext.getMultiContextMode());
            if (multiContextHandler == null) {
                throw new InvalidReportContextException("Unknown multi-context mode '" + reportContext.getMultiContextMode() + "'");
            }
            CommandResult multiContextExecutionPlanResult = childContextsResult.getResultForDependency("Multi-context execution plan");
            activeExecutionPlanContext = multiContextHandler.createExecutionPlanContext(reportContext, childExecutionPlanContexts);
            activeExecutionPlanContext.setDataSource(dataSource);
            activeExecutionPlan = multiContextHandler.createExecutionPlan();
            commandResult = multiContextExecutionPlanResult;

            childContextsPlaceholder.setSuccess(true).setComplete(true);
            childContextRemoteConfigurationPlaceholder.setSuccess(true).setComplete(true);
        } else {
            activeExecutionPlanContext = buildReportExecutionPlanContext(reportContext, dataSource, reportDefinition, remoteConfigurationResult);
            activeExecutionPlan = new SystemReportExecutionPlan(dimensionMapper);
        }

        activeExecutionPlanContext.setViewServerNodes(viewServerNodes);

        // run system report execution plan
        executionPlanRunner.executePlan(activeExecutionPlan, activeExecutionPlanContext, executionContext, systemCatalog, commandResult);

        if (reportContext.getOutput() != null) {
            // TODO: specify output name in context as well as operator name
            String operatorName = activeExecutionPlanContext.getOperatorName(reportContext.getOutput());
            if (operatorName == null) {
                throw new ViewServerException("Invalid report output '" + reportContext.getOutput() + "' requested.");
            }
            activeExecutionPlanContext.setInput(operatorName);
        }

        return activeExecutionPlanContext;
    }

    private ReportExecutionPlanContext buildReportExecutionPlanContext(ReportContext reportContext, IDataSource dataSource, ReportDefinition reportDefinition, CommandResult remoteConfigurationResult) {

        ReportExecutionPlanContext reportExecutionPlanContext = new ReportExecutionPlanContext();
        reportExecutionPlanContext.setInput(dataSource.getFinalOutput());
        reportExecutionPlanContext.setReportContext(reportContext);
        reportExecutionPlanContext.setGraphDefinition(reportDefinition);
        reportExecutionPlanContext.setDataSource(dataSource);
        reportExecutionPlanContext.setDistributionManager(distributionManager);
        reportExecutionPlanContext.setRemoteConfigurationResult(remoteConfigurationResult);

        return reportExecutionPlanContext;
    }

    protected IDataSource getDataSource(String name) {
        IDataSource dataSource = dataSourceRegistry.get(name);

        if(this.dataSourceRegistry.getStatus(name) != DataSourceStatus.INITIALIZED){
            throw new ViewServerException(String.format("DataSource %s has not been initialised", dataSource.getName()));
        }

        return dataSource;
    }

    protected ReportDefinition getReportDefinition(ReportContext reportContext) {
        if (!reportContext.getChildContexts().isEmpty()) {
            reportContext = reportContext.getChildContexts().get(0);
        }
        if(reportContext.getReportName() == null){
            throw new InvalidReportContextException("Report id must be set in the report context");
        }

        return reportRegistry.getReportById(reportContext.getReportName());
    }
}
