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

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.configurator.Configurator;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.ExecutionContext;
import io.viewserver.datasource.DataSourceStatus;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.InvalidReportContextException;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.ReportContextExecutionPlanContext;
import io.viewserver.execution.nodes.UnEnumNode;
import io.viewserver.network.IPeerSession;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import io.viewserver.util.ViewServerException;

import java.util.Collections;
import java.util.List;

public abstract class ReportContextHandler<TCommand> extends SubscriptionHandlerBase<TCommand>{
    protected IDataSourceRegistry dataSourceRegistry;
    protected ReportRegistry reportRegistry;
    protected DimensionMapper dimensionMapper;

    protected ReportContextHandler(Class<TCommand> clazz, DimensionMapper dimensionMapper, IDataSourceRegistry dataSourceRegistry, ReportRegistry reportRegistry, SubscriptionManager subscriptionManager, IDistributionManager distributionManager, Configurator configurator, ExecutionPlanRunner executionPlanRunner){
        super(clazz, subscriptionManager, distributionManager, configurator, executionPlanRunner);
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

    protected IConfiguratorSpec.OperatorSpec getUnEnumSpec(ExecutionContext executionContext, ICatalog catalog, ReportContextExecutionPlanContext executionPlanContext, CommandResult commandResult){
        // unenum
        UnEnumNode unEnumNode = new UnEnumNode("unenum", executionPlanContext.getDataSource())
                .withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(), Constants.IN);

        IConfiguratorSpec.OperatorSpec unEnumSpec = unEnumNode.getOperatorSpec(null, true);

        configurator.process(new IConfiguratorSpec() {
            @Override
            public List<OperatorSpec> getOperators() {
                return Collections.singletonList(unEnumSpec);
            }

            @Override
            public void reset() {
            }
        }, executionContext, catalog, commandResult);

        return unEnumSpec;
    }

    protected ICatalog getGraphNodesCatalog(IPeerSession peerSession) {
        final ICatalog systemCatalog = peerSession.getSystemCatalog();
        return systemCatalog.getChild("graphNodes");
    }
}
