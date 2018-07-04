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

import io.viewserver.execution.nodes.SortNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.sort.SortOperator;
import com.facebook.presto.sql.tree.*;

import java.util.List;

/**
 * Created by bemm on 19/11/15.
 */
public class SqlOrderByStep implements IExecutionPlanStep<SqlExecutionPlanContext> {
    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        final Query query = executionPlanContext.getQuery();
        final QuerySpecification body = (QuerySpecification) query.getQueryBody();
        final List<SortItem> orderBy = body.getOrderBy();
        final int count = orderBy.size();
        final SortNode orderByNode = executionPlanContext.getOrCreateOrderBy();
        for (int i = 0; i < count; i++) {
            final SortItem sortItem = orderBy.get(i);
            final Expression sortKey = sortItem.getSortKey();
            final String columnName;
            if (sortKey instanceof QualifiedNameReference) {
                columnName = ((QualifiedNameReference) sortKey).getName().getOriginalParts().get(0);
            } else if (sortKey instanceof DereferenceExpression) {
                final DereferenceExpression dereferenceExpression = (DereferenceExpression) sortKey;
                final Expression base = dereferenceExpression.getBase();
                if (base instanceof QualifiedNameReference) {
                    columnName = ((QualifiedNameReference)base).getName().toString() + "." + dereferenceExpression.getFieldName();
                } else {
                    throw new UnsupportedOperationException(String.format("Unhandled base expression type (%s) for %s",
                            base.getClass(), base));
                }
            } else {
                throw new UnsupportedOperationException(String.format("Unhandled expression type for %s (%s)",
                        sortKey, sortKey.getClass()));
            }
            final boolean descending = sortItem.getOrdering() == SortItem.Ordering.DESCENDING;
            final SortOperator.SortColumn sortColumn = new SortOperator.SortColumn(columnName,
                    descending);
            orderByNode.withColumnsToSort(sortColumn);
        }
    }
}
