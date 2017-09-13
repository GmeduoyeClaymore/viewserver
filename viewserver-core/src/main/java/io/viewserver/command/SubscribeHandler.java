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
import io.viewserver.configurator.Configurator;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.Options;
import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.messages.command.ISubscribeCommand;
import io.viewserver.messages.config.IProjectionConfig;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.IOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 07/10/2014.
 */
public class SubscribeHandler extends SubscriptionHandlerBase<ISubscribeCommand> {
    private static final Logger log = LoggerFactory.getLogger(SubscribeHandler.class);

    public SubscribeHandler(SubscriptionManager subscriptionManager, IDistributionManager distributionManager, Configurator configurator, ExecutionPlanRunner executionPlanRunner) {
        super(ISubscribeCommand.class, subscriptionManager, distributionManager, configurator, executionPlanRunner);
    }

    @Override
    protected void handleCommand(Command command, ISubscribeCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            IOperator operator = peerSession.getSessionCatalog().getOperator(data.getOperatorName());

            if(operator == null){
                throw new Exception(String.format("Operator %s does not exist in the catalog", data.getOperatorName()));
            }

            OptionsExecutionPlanContext optionsExecutionPlanContext = new OptionsExecutionPlanContext();
            Options options = null;
            if (data.getOptions() != null) {
                options = Options.fromMessage(data.getOptions());
                optionsExecutionPlanContext.setOptions(options);
            }
            String output = data.getOutputName();
            if (output == null) {
                output = Constants.OUT;
            }
            optionsExecutionPlanContext.setInput(operator.getPath(), output);
            optionsExecutionPlanContext.setDistributionManager(distributionManager);

            final IProjectionConfig projection = data.getProjection();
            if (projection != null) {
                optionsExecutionPlanContext.setProjectionConfig(io.viewserver.operators.projection.IProjectionConfig.fromDto(projection));
            }

            MultiCommandResult multiCommandResult = null;

            //for sorting, paging etc - only allow on the master node
            if(this.distributionManager.getNodeType().equals(IInitialiseSlaveCommand.Type.Master)) {
                multiCommandResult = MultiCommandResult.wrap("SubscribeHandler", commandResult);
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
