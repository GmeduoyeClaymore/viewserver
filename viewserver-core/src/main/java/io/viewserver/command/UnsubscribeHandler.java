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

import io.viewserver.messages.command.IUnsubscribeCommand;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.IInput;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.serialiser.SerialiserOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by paulg on 07/10/2014.
 */
public class UnsubscribeHandler extends CommandHandlerBase<IUnsubscribeCommand> {
    private static final Logger log = LoggerFactory.getLogger(UnsubscribeHandler.class);
    private SubscriptionManager subscriptionManager;

    public UnsubscribeHandler(SubscriptionManager subscriptionManager) {
        super(IUnsubscribeCommand.class);
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    protected void handleCommand(Command command, IUnsubscribeCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            SerialiserOperator serialiser = subscriptionManager.unregisterSubscription(peerSession.getConnectionId(), data.getSubscriptionId());
            if (serialiser == null) {
                commandResult.setSuccess(false).setMessage("No subscription " + data.getSubscriptionId() + " exists").setComplete(true);
                return;
            }

            tearDownUserOperators(serialiser);

            commandResult.setSuccess(true).setComplete(true);
        } catch (Throwable ex) {
            log.error("Failed to handle unsubscribe command", ex);
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
        }
    }

    private void tearDownUserOperators(IOperator operator) {
        ArrayList<IOperator> operatorsToTearDown = new ArrayList<>();
        for (IInput input : operator.getInputs().values()) {
            IOutput producer = input.getProducer();
            if (producer != null) {
                if (producer.getOwner().getCatalog() != operator.getCatalog()) {
                    continue;
                }
                Boolean isUserExecutionPlanNode = (Boolean) producer.getOwner().getMetadata("isUserExecutionPlanNode");
                if (isUserExecutionPlanNode != null && isUserExecutionPlanNode) {
                    operatorsToTearDown.add(producer.getOwner());
                }
            }
        }
        for (IOperator operatorToTearDown : operatorsToTearDown) {
            tearDownUserOperators(operatorToTearDown);
        }
        operator.tearDown();
    }
}
