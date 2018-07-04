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

import io.viewserver.Constants;
import io.viewserver.execution.nodes.*;
import io.viewserver.execution.steps.IExecutionPlanStep;

/**
 * Created by bemm on 19/11/15.
 */
public class SqlConnectionStep implements IExecutionPlanStep<SqlExecutionPlanContext> {
    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        FilterNode filter = executionPlanContext.getFilter();
        if (filter != null) {
            filter.withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(),
                    Constants.IN);
            executionPlanContext.setInput(filter.getName());
        }

        final CalcColNode preSummaryCalculations = executionPlanContext.getPreSummaryCalculations();
        if (preSummaryCalculations != null) {
            preSummaryCalculations.withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(),
                    Constants.IN);
            executionPlanContext.setInput(preSummaryCalculations.getName());
        }

        final GroupByNode groupBy = executionPlanContext.getGroupBy();
        if (groupBy != null) {
            groupBy.withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(),
                    Constants.IN);
            executionPlanContext.setInput(groupBy.getName());
        }

        final CalcColNode postSummaryCalculations = executionPlanContext.getPostSummaryCalculations();
        if (postSummaryCalculations != null) {
            postSummaryCalculations.withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(),
                    Constants.IN);
            executionPlanContext.setInput(postSummaryCalculations.getName());
        }

        final FilterNode having = executionPlanContext.getHaving();
        if (having != null) {
            having.withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(),
                    Constants.IN);
            executionPlanContext.setInput(having.getName());
        }

        SortNode orderBy = executionPlanContext.getOrderBy();
        if (orderBy != null) {
            orderBy.withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(),
                    Constants.IN);
            executionPlanContext.setInput(orderBy.getName());
        }

        ProjectionNode projection = executionPlanContext.getProjection();
        if (projection != null) {
            projection.withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(),
                    Constants.IN);
            executionPlanContext.setInput(projection.getName());
        }
    }
}
