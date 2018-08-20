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

import io.viewserver.catalog.ICatalog;
import io.viewserver.configurator.Configurator;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.*;
import io.viewserver.execution.context.DimensionExecutionPlanContext;
import io.viewserver.execution.plan.SystemDimensionExecutionPlan;
import io.viewserver.messages.command.ISubscribeDimensionCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.report.ReportContextRegistry;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bemm on 31/10/2014.
 */
public class SubscribeDimensionHandler extends ReportContextHandler<ISubscribeDimensionCommand> {
    private static final Logger log = LoggerFactory.getLogger(SubscribeDimensionHandler.class);
    private ReportContextRegistry reportContextRegistry;
    public SubscribeDimensionHandler(DimensionMapper dimensionMapper, IDataSourceRegistry dataSourceRegistry, ReportRegistry reportRegistry, SubscriptionManager subscriptionManager, Configurator configurator, IExecutionPlanRunner executionPlanRunner, ReportContextRegistry reportContextRegistry) {
        super(ISubscribeDimensionCommand.class, dimensionMapper, dataSourceRegistry, reportRegistry, subscriptionManager, configurator, executionPlanRunner);
        this.reportContextRegistry = reportContextRegistry;
    }

    @Override
    protected void handleCommand(Command command, ISubscribeDimensionCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {

            ReportContext reportContext = ReportContext.fromMessage(data.getReportContext());
            if(reportContext == null){
                throw new Exception(String.format("Report context must be specified"));
            }
            ReportDefinition reportDefinition = getReportDefinition(reportContext);
            if(reportDefinition == null){
                throw new Exception(String.format("Unable to find report definition for name %s",reportContext.getReportName()));
            }

            ParameterHelper parameterHelper = new ParameterHelper(reportDefinition, reportContext, peerSession.getExecutionContext().getDimensionMapper());

            String name = reportContext.getDataSourceName() == null || "".equals(reportContext.getDataSourceName()) ? reportDefinition.getDataSource() : reportContext.getDataSourceName();
            IDataSource dataSource = name == null ? null : getDataSource(name);

            if(reportContext.getDataSourceName() == null){
                throw new Exception(String.format("Data source name must be specified"));
            }

            if(dataSource.getDimension(data.getDimension()) == null){
                throw new Exception(String.format("Dimension %s does not exist in the dataSource %s", data.getDimension(), dataSource.getName()));
            }

            ICatalog catalog = reportContextRegistry.getOrCreateCatalogForContext(reportContext);

            Options options = Options.fromMessage(data.getOptions());

            SubscriptionUtils.substituteParamsInFilterExpression(peerSession, options);

            // build execution context
            DimensionExecutionPlanContext dimensionExecutionPlanContext = new DimensionExecutionPlanContext();
            dimensionExecutionPlanContext.setInput(dataSource.getFinalOutput());
            dimensionExecutionPlanContext.setReportContext(reportContext);
            dimensionExecutionPlanContext.setDataSourceName(reportContext.getDataSourceName());
            dimensionExecutionPlanContext.setOptions(options);
            dimensionExecutionPlanContext.setDataSource(dataSource);
            dimensionExecutionPlanContext.setDimension(data.getDimension());
            dimensionExecutionPlanContext.setParameterHelper(parameterHelper);


            MultiCommandResult multiCommandResult = MultiCommandResult.wrap("SubscribeDimensionHandler", commandResult);
            CommandResult systemPlanResult = multiCommandResult.getResultForDependency("System execution plan");
            CommandResult userPlanResult = multiCommandResult.getResultForDependency("User execution plan");

            // run system dimension execution plan
            SystemDimensionExecutionPlan systemDimensionExecutionPlan = new SystemDimensionExecutionPlan(dimensionMapper);
            executionPlanRunner.executePlan(systemDimensionExecutionPlan,
                    dimensionExecutionPlanContext,
                    peerSession.getExecutionContext(),
                    catalog,
                    systemPlanResult);


            //for sorting, paging etc
            String inputOperator = dimensionExecutionPlanContext.getInputOperator();
            if (inputOperator.charAt(0) != '/') {
                inputOperator = catalog.getOperatorByPath(inputOperator).getPath();
                dimensionExecutionPlanContext.setInput(inputOperator, dimensionExecutionPlanContext.getInputOutputName());
            }
            this.runUserExecutionPlan(dimensionExecutionPlanContext, options, command.getId(), peerSession, userPlanResult);

            // subscribe
            this.createSubscription(dimensionExecutionPlanContext, command.getId(), peerSession, options);
        } catch (Throwable ex) {
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
            log.error("Failed to subscribe to dimension", ex);
        }
    }
}
