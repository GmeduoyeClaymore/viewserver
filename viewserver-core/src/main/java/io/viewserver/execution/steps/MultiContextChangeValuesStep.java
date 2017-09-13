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

import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.plan.MultiContextExecutionPlanContext;
import io.viewserver.operators.calccol.CalcColOperator;

/**
 * Created by Paul Graves
 */
public class MultiContextChangeValuesStep implements IExecutionPlanStep<MultiContextExecutionPlanContext> {
    @Override
    public void execute(MultiContextExecutionPlanContext executionPlanContext) {
        CalcColNode changeValuesNode =  new CalcColNode("comparisonChangeValues")
                .withCalculations(
                        new CalcColOperator.CalculatedColumn("sourceId$1_bucket$2_$3_$4_$5PcChange", "if(sourceId0_bucket$2_$3_$4_$5 != 0 && sourceId$1_bucket$2_$3_$4_$5 != 0, if(sourceId0_bucket$2_$3_$4_$5 != 0, (( (sourceId0_bucket$2_$3_$4_$5 * 1.0f) / (sourceId$1_bucket$2_$3_$4_$5 * 1.0f) ) - 1.0f) * 100.0f, 100.0f), 0.0f)", "sourceId([1-9][0-9]*)_bucket([0-9][0-9]*)_([^_]+)_([^_]+)_([^_]+)(?<!Change)$"),
                        new CalcColOperator.CalculatedColumn("sourceId$1_bucket$2_$3_$4_$5AbsChange", "sourceId0_bucket$2_$3_$4_$5 - sourceId$1_bucket$2_$3_$4_$5", "sourceId([1-9][0-9]*)_bucket([0-9][0-9]*)_([^_]+)_([^_]+)_([^_]+)(?<!Change)$")
                )
                .withConnection(executionPlanContext.getInputOperator());

        executionPlanContext.addNodes(changeValuesNode);
        executionPlanContext.setInput(changeValuesNode.getName());
    }
}
