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

import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;

/**
 * Created by bemm on 01/04/2015.
 */
public class UserProjectionStep implements IExecutionPlanStep<OptionsExecutionPlanContext> {
    private int commandId;

    public UserProjectionStep(int commandId) {
        this.commandId = commandId;
    }

    @Override
    public void execute(OptionsExecutionPlanContext optionsExecutionPlanContext) {
        IProjectionConfig projectionConfig = optionsExecutionPlanContext.getProjectionConfig();
        if (projectionConfig == null) {
            return;
        }

        ProjectionNode projectionNode = new ProjectionNode("userProjection:" + commandId)
                .withMode(projectionConfig.getMode())
                .withProjectionColumns(projectionConfig.getProjectionColumns())
                .withMetadata("isUserExecutionPlanNode", true)
                .withConnection(optionsExecutionPlanContext.getInputOperator());
        optionsExecutionPlanContext.addNodes(projectionNode);
        optionsExecutionPlanContext.setInput(projectionNode.getName());
    }
}
