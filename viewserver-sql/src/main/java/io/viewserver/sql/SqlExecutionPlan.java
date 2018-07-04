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

package io.viewserver.sql;

import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.execution.plan.IExecutionPlan;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.group.summary.SummaryRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bemm on 19/11/15.
 */
public class SqlExecutionPlan implements IExecutionPlan<SqlExecutionPlanContext> {
    private SummaryRegistry summaryRegistry;
    private IExecutionPlanRunner executionPlanRunner;

    public SqlExecutionPlan(SummaryRegistry summaryRegistry, IExecutionPlanRunner executionPlanRunner) {
        this.summaryRegistry = summaryRegistry;
        this.executionPlanRunner = executionPlanRunner;
    }

    @Override
    public List<IExecutionPlanStep<SqlExecutionPlanContext>> getSteps() {
        return Arrays.asList(
                new SqlParametersStep(),
                new SqlPrefixStep(),
                new SqlFromStep(summaryRegistry, executionPlanRunner),
                new SqlGroupByStep(),
                new SqlSelectStep(summaryRegistry),
                new SqlWhereStep(),
                new SqlHavingStep(summaryRegistry),
                new SqlOrderByStep(),
                new SqlLimitStep(),
                new SqlConnectionStep()
        );
    }
}
