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
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.DataSourceStatus;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.plan.IExecutionPlan;
import io.viewserver.execution.plan.SystemReportExecutionPlan;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.util.ViewServerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bemm on 05/10/15.
 */
public class SystemReportExecutor {
    private final DimensionMapper dimensionMapper;
    private final IExecutionPlanRunner executionPlanRunner;
    private final IDataSourceRegistry dataSourceRegistry;
    private final ReportRegistry reportRegistry;

    public SystemReportExecutor(
                                DimensionMapper dimensionMapper,
                                IExecutionPlanRunner executionPlanRunner,
                                IDataSourceRegistry dataSourceRegistry,
                                ReportRegistry reportRegistry) {
        this.dimensionMapper = dimensionMapper;
        this.executionPlanRunner = executionPlanRunner;
        this.dataSourceRegistry = dataSourceRegistry;
        this.reportRegistry = reportRegistry;
    }

    public ReportContextExecutionPlanContext executeContext(ReportContext reportContext, IExecutionContext executionContext, ICatalog systemCatalog, CommandResult commandResult) {
        IExecutionPlan activeExecutionPlan;
        ReportContextExecutionPlanContext activeExecutionPlanContext;

        ReportDefinition reportDefinition = getReportDefinition(reportContext);
        IDataSource dataSource = reportDefinition.getDataSource() == null ? null : getDataSource(reportDefinition.getDataSource());

        activeExecutionPlanContext = buildReportExecutionPlanContext(reportContext, dataSource, reportDefinition);
        activeExecutionPlan = new SystemReportExecutionPlan(dimensionMapper, reportRegistry);

        executionPlanRunner.executePlan(activeExecutionPlan, activeExecutionPlanContext, executionContext, systemCatalog, commandResult);

        if (reportContext.getOutput() != null) {
            String operatorName = activeExecutionPlanContext.getOperatorName(reportContext.getOutput());
            if (operatorName == null) {
                throw new ViewServerException("Invalid report output '" + reportContext.getOutput() + "' requested.");
            }
            activeExecutionPlanContext.setInput(operatorName);
        }

        return activeExecutionPlanContext;
    }

    private ReportExecutionPlanContext buildReportExecutionPlanContext(ReportContext reportContext, IDataSource dataSource, ReportDefinition reportDefinition) {

        ReportExecutionPlanContext reportExecutionPlanContext = new ReportExecutionPlanContext();
        if(dataSource != null) {
            reportExecutionPlanContext.setInput(dataSource.getFinalOutput());
        }
        reportExecutionPlanContext.setReportContext(reportContext);
        reportExecutionPlanContext.setGraphDefinition(reportDefinition);
        reportExecutionPlanContext.setDataSource(dataSource);

        return reportExecutionPlanContext;
    }

    protected IDataSource getDataSource(String name) {
        IDataSource dataSource = dataSourceRegistry.get(name);
        DataSourceStatus status = this.dataSourceRegistry.getStatus(name);
        if(status != DataSourceStatus.INITIALIZED){
            throw new ViewServerException(String.format("DataSource %s is not ready and is in status %s", name, status));
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
