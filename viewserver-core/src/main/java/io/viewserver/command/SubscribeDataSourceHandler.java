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
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.datasource.SlaveDataSource;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.Options;
import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.messages.command.ISubscribeDataSourceCommand;
import io.viewserver.messages.config.IProjectionConfig;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 07/10/2014.
 */
public class SubscribeDataSourceHandler extends SubscriptionHandlerBase<ISubscribeDataSourceCommand> {
    private static final Logger log = LoggerFactory.getLogger(SubscribeDataSourceHandler.class);
    private IDataSourceRegistry<SlaveDataSource> dataSourceRegistry;

    public SubscribeDataSourceHandler(IDataSourceRegistry dataSourceRegistry, SubscriptionManager subscriptionManager, IDistributionManager distributionManager, Configurator configurator, ExecutionPlanRunner executionPlanRunner) {
        super(ISubscribeDataSourceCommand.class, subscriptionManager, distributionManager, configurator, executionPlanRunner);
        this.dataSourceRegistry = dataSourceRegistry;
    }

    @Override
    protected void handleCommand(Command command, ISubscribeDataSourceCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {

            IDataSource dataSource = dataSourceRegistry.get(data.getDataSourceName());
            MultiCommandResult multiCommandResult = null;
            final ICatalog graphNodesCatalog = getGraphNodesCatalog(peerSession);

            OptionsExecutionPlanContext optionsExecutionPlanContext = new OptionsExecutionPlanContext();
            Options options = null;
            if (data.getOptions() != null) {
                options = Options.fromMessage(data.getOptions());
                optionsExecutionPlanContext.setOptions(options);
            }

            SubscriptionUtils.substituteParamsInFilterExpression(peerSession, options);

            optionsExecutionPlanContext.setDataSource(dataSource);
            optionsExecutionPlanContext.setInput(dataSource.getFinalOutput());
            optionsExecutionPlanContext.setDistributionManager(distributionManager);

            final IProjectionConfig projection = data.getProjection();
            if (projection != null) {
                optionsExecutionPlanContext.setProjectionConfig(io.viewserver.operators.projection.IProjectionConfig.fromDto(projection));
            }

            //for sorting, paging etc - only allow on the master node
            if(this.distributionManager.getNodeType().equals(IInitialiseSlaveCommand.Type.Master)) {
                multiCommandResult = MultiCommandResult.wrap("SubscribeHandler", commandResult);

                CommandResult unenumeratorResult = multiCommandResult.getResultForDependency("Unenumerator");
                IConfiguratorSpec.OperatorSpec unEnumSpec = this.getUnEnumSpec(peerSession.getExecutionContext(),
                        graphNodesCatalog,
                        optionsExecutionPlanContext,
                        unenumeratorResult);
                optionsExecutionPlanContext.setInput(unEnumSpec.getName());

                String inputOperator = optionsExecutionPlanContext.getInputOperator();
                if (inputOperator.charAt(0) != '/') {
                    inputOperator = graphNodesCatalog.getOperator(inputOperator).getPath();
                    optionsExecutionPlanContext.setInput(inputOperator, optionsExecutionPlanContext.getInputOutputName());
                }

                CommandResult userPlanResult = multiCommandResult.getResultForDependency("User execution plan");
                this.runUserExecutionPlan(optionsExecutionPlanContext, options, command.getId(), peerSession, userPlanResult);
            }

            this.createSubscription(optionsExecutionPlanContext, command.getId(), peerSession, options);

            if (multiCommandResult == null) {
                commandResult.setSuccess(true).setComplete(true);
            }
        } catch (Throwable ex) {
            log.error("Failed to handle subscribe command", ex);
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
        }


    }
}
