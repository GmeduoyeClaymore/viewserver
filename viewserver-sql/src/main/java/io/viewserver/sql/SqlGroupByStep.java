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

import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import com.facebook.presto.sql.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 20/11/15.
 */
public class SqlGroupByStep implements IExecutionPlanStep<SqlExecutionPlanContext> {
    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        final Query query = executionPlanContext.getQuery();
        final QuerySpecification body = (QuerySpecification) query.getQueryBody();
        final List<GroupingElement> groupBy = body.getGroupBy();
        if (groupBy.isEmpty()) {
            return;
        }

        final GroupByNode groupByNode = executionPlanContext.getOrCreateGroupBy();
        final List<String> groupByColumns = new ArrayList<>();
        final int count = groupBy.size();
        for (int i = 0; i < count; i++) {
            final GroupingElement groupingElement = groupBy.get(i);
            if (groupingElement instanceof SimpleGroupBy) {
                final SimpleGroupBy simpleGroupBy = (SimpleGroupBy) groupingElement;
                final List<Expression> columnExpressions = simpleGroupBy.getColumnExpressions();
                final int expressionCount = columnExpressions.size();
                for (int j = 0; j < expressionCount; j++) {
                    Expression expression = columnExpressions.get(j);
                    final String columnName;
                    if (expression instanceof QualifiedNameReference) {
                        columnName = ((QualifiedNameReference) expression).getName().getOriginalParts().get(0);
                    } else if (expression instanceof DereferenceExpression) {
                        final DereferenceExpression dereferenceExpression = (DereferenceExpression) expression;
                        final Expression base = dereferenceExpression.getBase();
                        if (base instanceof QualifiedNameReference) {
                            columnName = ((QualifiedNameReference)base).getName().toString() + "." + dereferenceExpression.getFieldName();
                        } else {
                            throw new UnsupportedOperationException(String.format("Unhandled base expression type (%s) for %s",
                                    base.getClass(), base));
                        }
                    } else {
                        throw new UnsupportedOperationException(String.format("Unsupported expression type (%s) in GROUP BY for %s",
                                expression.getClass(), expression));
                    }
                    groupByColumns.add(columnName);
                }
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported grouping element type (%s) for %s",
                        groupingElement.getClass(), groupingElement));
            }
        }
        groupByNode.withGroupByColumns(groupByColumns.toArray(new String[groupByColumns.size()]));
    }
}
