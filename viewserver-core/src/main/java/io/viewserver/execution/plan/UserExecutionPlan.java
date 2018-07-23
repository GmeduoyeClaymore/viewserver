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

package io.viewserver.execution.plan;

import io.viewserver.execution.context.OptionsExecutionPlanContext;
import io.viewserver.execution.steps.*;

import java.util.Arrays;
import java.util.List;


public class UserExecutionPlan implements IExecutionPlan<OptionsExecutionPlanContext> {
    private int commandId;

    public UserExecutionPlan(int commandId){
        super();
        this.commandId = commandId;
    }

    @Override
    public List<IExecutionPlanStep<OptionsExecutionPlanContext>> getSteps() {
        return Arrays.asList(
                new UserParametersStep(),
                new UserFilterStep(commandId),
                new UserProjectionStep(commandId),
                new UserUnEnumStep(commandId)
                //new SortAndPageStep(commandId)

                );
    }
}
