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
import io.viewserver.execution.nodes.UserSortNode;

/**
 * Created by bemm on 31/10/2014.
 */
public class SortAndPageStep implements IExecutionPlanStep<OptionsExecutionPlanContext> {
    private int commandId;

    public SortAndPageStep(int commandId){
        this.commandId = commandId;
    }

    @Override
    public void execute(OptionsExecutionPlanContext optionsExecutionPlanContext) {
        final Options options = optionsExecutionPlanContext.getOptions();

        if(options != null) {

            UserSortNode sortNode = (UserSortNode)new UserSortNode(String.format("sort:%d", commandId))
                    .withColumnName(options.getColumnName())
                    .withColumnsToSort(options.getColumnsToSort())
                    .withOffset(options.getOffset())
                    .withLimit(options.getLimit())
                    .withMetadata("isUserExecutionPlanNode", true)
                    .withConnection(optionsExecutionPlanContext.getInputOperator(), optionsExecutionPlanContext.getInputOutputName(), Constants.IN);

            optionsExecutionPlanContext.addNodes(sortNode);
            optionsExecutionPlanContext.setInput(sortNode.getName());
        }
    }
}
