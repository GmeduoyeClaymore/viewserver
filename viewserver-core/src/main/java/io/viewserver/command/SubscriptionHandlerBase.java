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
import io.viewserver.core.IExecutionContext;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.Options;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.nodes.UnEnumNode;
import io.viewserver.execution.plan.UserExecutionPlan;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.serialiser.SerialiserOperator;
import io.viewserver.util.ViewServerException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by nickc on 31/10/2014.
 */


public abstract class SubscriptionHandlerBase<TCommand> extends CommandHandlerBase<TCommand> {
    protected final ExecutionPlanRunner executionPlanRunner;
    protected final SubscriptionManager subscriptionManager;
    protected final IDistributionManager distributionManager;
    protected final Configurator configurator;
//    private final IPool<MessageMessage.MessageDto.Builder> messagePool;

    protected SubscriptionHandlerBase(Class<TCommand> clazz, SubscriptionManager subscriptionManager, IDistributionManager distributionManager,
                                      Configurator configurator, ExecutionPlanRunner executionPlanRunner) {
        super(clazz);
        this.subscriptionManager = subscriptionManager;
        this.distributionManager = distributionManager;
        this.configurator = configurator;
        this.executionPlanRunner = executionPlanRunner;

//        messagePool = new Pool<>(MessageMessage.MessageDto::newBuilder, 128, MessageMessage.MessageDto.Builder::clear);
    }

    protected void runUserExecutionPlan(IExecutionPlanContext executionPlanContext, Options options, int commandId, IPeerSession peerSession, CommandResult commandResult){
        // run user report execution plan
        UserExecutionPlan userExecutionPlan = new UserExecutionPlan(commandId);
        OptionsExecutionPlanContext optionsExecutionPlanContext = (OptionsExecutionPlanContext) executionPlanContext;
        optionsExecutionPlanContext.setOptions(options);
        executionPlanRunner.executePlan(userExecutionPlan, optionsExecutionPlanContext, peerSession.getExecutionContext(), peerSession.getSessionCatalog(), commandResult);
    }

    protected void createSubscription(IExecutionPlanContext executionPlanContext, int commandId, IPeerSession peerSession, Options options){
        IOperator operator = peerSession.getSessionCatalog().getOperator(executionPlanContext.getInputOperator());
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

    protected IConfiguratorSpec.OperatorSpec getUnEnumSpec(IExecutionContext executionContext, ICatalog catalog, OptionsExecutionPlanContext executionPlanContext, CommandResult commandResult){
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
