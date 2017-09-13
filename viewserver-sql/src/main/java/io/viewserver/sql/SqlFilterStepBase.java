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

import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.filter.FilterOperator;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.Query;
import com.facebook.presto.sql.tree.QuerySpecification;

import java.util.Optional;

/**
 * Created by nick on 19/11/15.
 */
public abstract class SqlFilterStepBase implements IExecutionPlanStep<SqlExecutionPlanContext> {
    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        final Query query = executionPlanContext.getQuery();
        final QuerySpecification body = (QuerySpecification) query.getQueryBody();
        final Optional<Expression> filterExpression = getFilterExpression(body, executionPlanContext);
        if (!filterExpression.isPresent()) {
            return;
        }

        FilterNode filterNode = new FilterNode(getName())
                .withMode(FilterOperator.FilterMode.Filter)
                .withExpression(SqlExpressionFormatter.formatExpression(filterExpression.get()));
        executionPlanContext.getGraphNodes().add(filterNode);
        updateContext(executionPlanContext, filterNode);
    }

    protected abstract String getName();

    protected abstract Optional<Expression> getFilterExpression(QuerySpecification body, SqlExecutionPlanContext executionPlanContext);

    protected abstract void updateContext(SqlExecutionPlanContext executionPlanContext, FilterNode filter);
}
