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
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.nodes.UnionNode;
import io.viewserver.execution.plan.MultiContextExecutionPlanContext;

public class MultiContextUnionStep implements IExecutionPlanStep<MultiContextExecutionPlanContext> {

    @Override
    public void execute(MultiContextExecutionPlanContext multiContextExecutionPlanContext) {
        String output = multiContextExecutionPlanContext.getReportContext().getOutput();
        if (output != null) {
            // check if the custom output is part of the child report(s) - if it is, we will union them here
            // if it isn't, then the custom output is part of the parent report graph
            IExecutionPlanContext firstChildContext = multiContextExecutionPlanContext.getExecutionPlanContexts().get(0);
            if (firstChildContext.getOperatorName(output) != null) {

                UnionNode unionNode = new UnionNode(output);

                int counter = 0;
                for (IExecutionPlanContext childContext : multiContextExecutionPlanContext.getExecutionPlanContexts()) {
                    String operatorName = childContext.getOperatorName(output);
                    String inputName = Constants.IN + counter;
                    unionNode.withInput(inputName, counter)
                            .withConnection(operatorName, Constants.OUT, inputName);

                    counter++;
                }

                multiContextExecutionPlanContext.addNodes(unionNode);
            }
        }

        UnionNode unionNode = new UnionNode("reportUnion");
        int counter = 0;

        for (IExecutionPlanContext reportContextExecutionPlanContext : multiContextExecutionPlanContext.getExecutionPlanContexts()) {
            String inputName = Constants.IN + counter;
            unionNode.withInput(inputName, counter);
            unionNode.withConnection(reportContextExecutionPlanContext.getInputOperator(), reportContextExecutionPlanContext.getInputOutputName(), inputName);
            counter++;
        }

        multiContextExecutionPlanContext.addNodes(unionNode);
        multiContextExecutionPlanContext.setInput(unionNode.getName());
    }
}
