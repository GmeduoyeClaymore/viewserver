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
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.execution.Options;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.plan.UserExecutionPlan;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.serialiser.SerialiserOperator;
import io.viewserver.util.ViewServerException;

import java.util.UUID;

/**
 * Created by bemm on 31/10/2014.
 */


public abstract class SubscriptionHandlerBase<TCommand> extends CommandHandlerBase<TCommand> {
    protected final IExecutionPlanRunner executionPlanRunner;
    protected final SubscriptionManager subscriptionManager;
    protected final Configurator configurator;

    protected SubscriptionHandlerBase(Class<TCommand> clazz, SubscriptionManager subscriptionManager,
                                      Configurator configurator, IExecutionPlanRunner executionPlanRunner) {
        super(clazz);
        this.subscriptionManager = subscriptionManager;
        this.configurator = configurator;
        this.executionPlanRunner = executionPlanRunner;
    }

    protected void runUserExecutionPlan(IExecutionPlanContext executionPlanContext, Options options, int commandId, IPeerSession peerSession, CommandResult commandResult){
        UserExecutionPlan userExecutionPlan = new UserExecutionPlan(commandId);
        OptionsExecutionPlanContext optionsExecutionPlanContext = (OptionsExecutionPlanContext) executionPlanContext;
        optionsExecutionPlanContext.setOptions(options);
        executionPlanRunner.executePlan(userExecutionPlan, optionsExecutionPlanContext, peerSession.getExecutionContext(), peerSession.getSessionCatalog(), commandResult);

        if(!commandResult.isSuccess()){
            throw new RuntimeException("Problem running data source execution plan");
        }
    }

    protected void createSubscription(IExecutionPlanContext executionPlanContext, int commandId, IPeerSession peerSession, Options options){
        IOperator operator = peerSession.getSessionCatalog().getOperatorByPath(executionPlanContext.getInputOperator());
        if (operator == null) {
            throw new ViewServerException("Invalid output '" + executionPlanContext.getInputOperator() + "' in execution plan");
        }

        SerialiserOperator serialiser = new SerialiserOperator(UUID.randomUUID().toString(),
                peerSession.getExecutionContext(),
                peerSession.getSessionCatalog(),
                peerSession.getMessageManager(),
                subscriptionManager,
                peerSession.getConnectionId(),
                commandId,
                //messagePool,
                options);

        operator.getOutput(executionPlanContext.getInputOutputName()).plugIn(serialiser.getInput());
    }



    protected ICatalog getGraphNodesCatalog(IPeerSession peerSession) {
        final ICatalog systemCatalog = peerSession.getSystemCatalog();
        return systemCatalog.getChild("graphNodes");
    }
}
