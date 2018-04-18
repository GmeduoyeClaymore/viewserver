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

import io.viewserver.configurator.Configurator;
import io.viewserver.datasource.DataSourceStatus;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.execution.InvalidReportContextException;
import io.viewserver.execution.ReportContext;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.util.ViewServerException;

public abstract class ReportContextHandler<TCommand> extends SubscriptionHandlerBase<TCommand>{
    protected IDataSourceRegistry dataSourceRegistry;
    protected ReportRegistry reportRegistry;
    protected DimensionMapper dimensionMapper;

    protected ReportContextHandler(Class<TCommand> clazz, DimensionMapper dimensionMapper, IDataSourceRegistry dataSourceRegistry, ReportRegistry reportRegistry, SubscriptionManager subscriptionManager, Configurator configurator, IExecutionPlanRunner executionPlanRunner){
        super(clazz, subscriptionManager, configurator, executionPlanRunner);
        this.dimensionMapper = dimensionMapper;
        this.dataSourceRegistry = dataSourceRegistry;
        this.reportRegistry = reportRegistry;
    }

    protected IDataSource getDataSource(String name) {
        IDataSource dataSource = dataSourceRegistry.get(name);

        if(this.dataSourceRegistry.getStatus(name) != DataSourceStatus.INITIALIZED){
            throw new ViewServerException(String.format("DataSource %s has not been initialised", dataSource.getName()));
        }

        return dataSource;
    }

    protected ReportDefinition getReportDefinition(ReportContext reportContext) {
        if(reportContext.getReportName() == null){
            throw new InvalidReportContextException("Report id must be set in the report context");
        }

        return reportRegistry.getReportById(reportContext.getReportName());
    }
}
