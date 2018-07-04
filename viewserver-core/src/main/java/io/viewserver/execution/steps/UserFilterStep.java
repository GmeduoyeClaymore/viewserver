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

package io.viewserver.execution.steps;

import io.viewserver.Constants;
import io.viewserver.execution.Options;
import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.UserFilterNode;

/**
 * Created by bemm on 06/03/2015.
 */
public class UserFilterStep implements IExecutionPlanStep<OptionsExecutionPlanContext> {
    private int commandId;

    public UserFilterStep(int commandId) {
        this.commandId = commandId;
    }

    @Override
    public void execute(OptionsExecutionPlanContext optionsExecutionPlanContext) {
        Options options = optionsExecutionPlanContext.getOptions();
        FilterNode userFilter = new UserFilterNode("userFilter:" + commandId)
                .withMetadata("isUserExecutionPlanNode", true)
                .withConnection(optionsExecutionPlanContext.getInputOperator(), optionsExecutionPlanContext.getInputOutputName(), Constants.IN);
        if (options != null) {
            userFilter.setMode(options.getFilterMode());
            userFilter.setExpression(options.getFilterExpression());
        }
        optionsExecutionPlanContext.addNodes(userFilter);
        optionsExecutionPlanContext.setInput(userFilter.getName());
    }
}
