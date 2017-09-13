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

import io.viewserver.datasource.DimensionMapper;
import io.viewserver.execution.context.DimensionExecutionPlanContext;
import io.viewserver.execution.steps.DimensionGroupByStep;
import io.viewserver.execution.steps.DimensionValuesStep;
import io.viewserver.execution.steps.IExecutionPlanStep;

import java.util.Arrays;
import java.util.List;

public class SystemDimensionExecutionPlan implements IExecutionPlan<DimensionExecutionPlanContext> {

    private DimensionMapper dimensionMapper;

    public SystemDimensionExecutionPlan(DimensionMapper dimensionMapper) {
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public List<IExecutionPlanStep<DimensionExecutionPlanContext>> getSteps() {
        return Arrays.asList(
                new DimensionValuesStep(dimensionMapper),
                new DimensionGroupByStep()
        );

    }
}
