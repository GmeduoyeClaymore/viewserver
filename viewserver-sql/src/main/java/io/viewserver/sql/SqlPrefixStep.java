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

import io.viewserver.execution.steps.IExecutionPlanStep;
import com.facebook.presto.sql.tree.*;

/**
 * Created by nick on 19/11/15.
 */
public class SqlPrefixStep implements IExecutionPlanStep<SqlExecutionPlanContext> {
    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        Query query = executionPlanContext.getQuery();
        VisitorContext visitorContext = new VisitorContext();
        query.accept(new DefaultTraversalVisitor<Void, VisitorContext>() {
            @Override
            protected Void visitAllColumns(AllColumns node, VisitorContext context) {
                context.usingPrefixes |= node.getPrefix().isPresent();
                return null;
            }

            @Override
            protected Void visitDereferenceExpression(DereferenceExpression node, VisitorContext context) {
                context.usingPrefixes = true;
                return null;
            }

            @Override
            protected Void visitAliasedRelation(AliasedRelation node, VisitorContext context) {
                if (node.getRelation() instanceof Table) {
                    Table table = (Table) node.getRelation();
                    executionPlanContext.addPrefix(node.getAlias(), table.getName().toString());
                }
                return super.visitAliasedRelation(node, context);
            }
        }, visitorContext);
        executionPlanContext.setUsingPrefixes(visitorContext.usingPrefixes);
    }

    private class VisitorContext {
        private boolean usingPrefixes;
    }
}
