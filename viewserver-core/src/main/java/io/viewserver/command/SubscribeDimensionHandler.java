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
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.Options;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.DimensionExecutionPlanContext;
import io.viewserver.execution.plan.SystemDimensionExecutionPlan;
import io.viewserver.messages.command.ISubscribeDimensionCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.OperatorFactoryRegistry;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 31/10/2014.
 */
public class SubscribeDimensionHandler extends ReportContextHandler<ISubscribeDimensionCommand> {
    private static final Logger log = LoggerFactory.getLogger(SubscribeDimensionHandler.class);

    public SubscribeDimensionHandler(DimensionMapper dimensionMapper, IDataSourceRegistry dataSourceRegistry, ReportRegistry reportRegistry, SubscriptionManager subscriptionManager, IDistributionManager distributionManager, OperatorFactoryRegistry operatorFactoryRegistry, Configurator configurator, ExecutionPlanRunner executionPlanRunner) {
        super(ISubscribeDimensionCommand.class, dimensionMapper, dataSourceRegistry, reportRegistry, subscriptionManager, distributionManager, configurator, executionPlanRunner);
    }

    @Override
    protected void handleCommand(Command command, ISubscribeDimensionCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            ReportContext reportContext = ReportContext.fromMessage(data.getReportContext());
            ReportDefinition reportDefinition = getReportDefinition(reportContext);
            IDataSource dataSource = getDataSource(reportDefinition.getDataSource());

            if(dataSource.getDimension(data.getDimension()) == null){
                throw new Exception(String.format("Dimension %s does not exist in the dataSource %s", data.getDimension(), dataSource.getName()));
            }

            Options options = Options.fromMessage(data.getOptions());

            SubscriptionUtils.substituteParamsInFilterExpression(peerSession, options);

            // build execution context
            DimensionExecutionPlanContext dimensionExecutionPlanContext = new DimensionExecutionPlanContext();
            dimensionExecutionPlanContext.setInput(dataSource.getFinalOutput());
            dimensionExecutionPlanContext.setReportContext(reportContext);
            dimensionExecutionPlanContext.setOptions(options);
            dimensionExecutionPlanContext.setDataSource(dataSource);
            dimensionExecutionPlanContext.setDimension(data.getDimension());
            dimensionExecutionPlanContext.setDistributionManager(distributionManager);
            dimensionExecutionPlanContext.setParameterHelper(new ParameterHelper(dimensionExecutionPlanContext.getReportContext()));

            MultiCommandResult multiCommandResult = MultiCommandResult.wrap("SubscribeDimensionHandler", commandResult);
            CommandResult systemPlanResult = multiCommandResult.getResultForDependency("System execution plan");
            CommandResult unenumeratorResult = multiCommandResult.getResultForDependency("Unenumerator");
            CommandResult userPlanResult = multiCommandResult.getResultForDependency("User execution plan");

            final ICatalog graphNodesCatalog = getGraphNodesCatalog(peerSession);

            // run system dimension execution plan
            SystemDimensionExecutionPlan systemDimensionExecutionPlan = new SystemDimensionExecutionPlan(dimensionMapper);
            executionPlanRunner.executePlan(systemDimensionExecutionPlan,
                    dimensionExecutionPlanContext,
                    peerSession.getExecutionContext(),
                    graphNodesCatalog,
                    systemPlanResult);

            //converts int values back to strings
            IConfiguratorSpec.OperatorSpec unEnumSpec = this.getUnEnumSpec(peerSession.getExecutionContext(),
                    graphNodesCatalog,
                    dimensionExecutionPlanContext,
                    unenumeratorResult);
            dimensionExecutionPlanContext.setInput(unEnumSpec.getName());

            //for sorting, paging etc
            String inputOperator = dimensionExecutionPlanContext.getInputOperator();
            if (inputOperator.charAt(0) != '/') {
                inputOperator = graphNodesCatalog.getOperator(inputOperator).getPath();
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
