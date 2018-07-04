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
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.ExpressionTreeRewriter;
import com.facebook.presto.sql.tree.QuerySpecification;

import java.util.Optional;

/**
 * Created by bemm on 20/11/15.
 */
public class SqlWhereStep extends SqlFilterStepBase {
    @Override
    protected String getName() {
        return "where";
    }

    @Override
    protected Optional<Expression> getFilterExpression(QuerySpecification body, SqlExecutionPlanContext executionPlanContext) {
        if (body.getWhere().isPresent()) {
            final SupportedExpressionRewriter<Void> rewriter = new SupportedExpressionRewriter<>();
            ExpressionTreeRewriter.rewriteWith(rewriter, body.getWhere().get());
        }
        return body.getWhere();
    }

    @Override
    protected void updateContext(SqlExecutionPlanContext executionPlanContext, FilterNode filter) {
        executionPlanContext.setFilter(filter);
    }
}
