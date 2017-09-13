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

import io.viewserver.configurator.IConfigurator;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.Options;
import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.plan.UserExecutionPlan;
import io.viewserver.messages.command.IOptions;
import io.viewserver.messages.command.IUpdateSubscriptionCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nickc on 31/10/2014.
 */
public class UpdateSubscriptionHandler extends CommandHandlerBase<IUpdateSubscriptionCommand> {
    private static final Logger log = LoggerFactory.getLogger(UpdateSubscriptionHandler.class);
    private final ExecutionPlanRunner executionPlanRunner;

    public UpdateSubscriptionHandler(IConfigurator configurator, IDistributionManager distributionManager) {
        super(IUpdateSubscriptionCommand.class);
        this.executionPlanRunner = new ExecutionPlanRunner(configurator, distributionManager);
    }

    @Override
    protected void handleCommand(Command command, IUpdateSubscriptionCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            OptionsExecutionPlanContext executionPlanContext = new OptionsExecutionPlanContext();
            final IOptions options = data.getOptions();
            if (options != null) {
                executionPlanContext.setOptions(Options.fromMessage(options));
            }

            UserExecutionPlan userExecutionPlan = new UserExecutionPlan(data.getCommandId());
            executionPlanRunner.executePlan(userExecutionPlan, executionPlanContext, peerSession.getExecutionContext(), peerSession.getSessionCatalog(), commandResult);
        } catch (Throwable ex) {
            log.error("Failed to update report", ex);
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
        }
    }
}
