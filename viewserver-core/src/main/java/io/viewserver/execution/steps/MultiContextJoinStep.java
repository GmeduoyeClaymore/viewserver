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

import io.viewserver.Constants;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.IExecutionPlanContext;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.plan.MultiContextExecutionPlanContext;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.expression.parser.ExpressionVisitorImpl;
import io.viewserver.expression.tree.IExpression;
import io.viewserver.expression.tree.IExpressionString;
import io.viewserver.expression.tree.literal.LiteralString;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.operators.join.IColumnNameResolver;

import java.util.List;
import java.util.Map;

/**
 * Created by nick on 16/03/2015.
 */
public class MultiContextJoinStep implements IExecutionPlanStep<MultiContextExecutionPlanContext> {
    private IExpressionParser expressionParser;

    public MultiContextJoinStep(IExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public void execute(MultiContextExecutionPlanContext executionPlanContext) {
        ReportContext reportContext = executionPlanContext.getReportContext();

        ValueLists.IStringList aggregators = (ValueLists.IStringList)reportContext.getParameterValue("aggregators");

        ValueLists.IStringList leftJoinColumns = (ValueLists.IStringList)reportContext.getParameterValue("leftJoinColumns");

        ValueLists.IStringList rightJoinColumns = (ValueLists.IStringList)reportContext.getParameterValue("rightJoinColumns");

        IColumnNameResolver columnNameResolver = (originalName, isLeft) -> null;
        ValueLists.IStringList joinColumnNameResolverExpressionValues = (ValueLists.IStringList)reportContext.getParameterValue("joinColumnNameResolverExpression");
        if (joinColumnNameResolverExpressionValues != null && !joinColumnNameResolverExpressionValues.isEmpty()) {
            columnNameResolver = new ExpressionColumnNameResolver(joinColumnNameResolverExpressionValues.get(0),
                    executionPlanContext.getExecutionContext().getFunctionRegistry(),
                    null);
        }

        List<IExecutionPlanContext> childContexts = executionPlanContext.getExecutionPlanContexts();
        String leftInput = childContexts.get(0).getInputOperator();
        for (int i = 1; i < childContexts.size(); i++) {
            String rightInput = childContexts.get(i).getInputOperator();
            JoinNode joinNode = new JoinNode("join" + i)
                    .withLeftJoinColumns(leftJoinColumns != null ? leftJoinColumns : aggregators)
                    .withLeftJoinOuter()
                    .withRightJoinColumns(rightJoinColumns != null ? rightJoinColumns : aggregators)
                    .withRightJoinOuter()
                    .withConnection(leftInput, Constants.OUT, "left")
                    .withConnection(rightInput, Constants.OUT, "right")
                    .withColumnNameResolver(columnNameResolver);
            executionPlanContext.addNodes(joinNode);
            executionPlanContext.setInput(joinNode.getName());
            leftInput = joinNode.getName();
        }
    }

    private class ExpressionColumnNameResolver implements IColumnNameResolver {
        private final ExpressionVisitor expressionVisitor;
        private String expression;

        public ExpressionColumnNameResolver(String expression, FunctionRegistry functionRegistry, Map<String, String> columnAliases) {
            this.expression = expression;
            expressionVisitor = new ExpressionVisitor(functionRegistry, columnAliases);
        }

        @Override
        public String resolveColumnName(String originalName, boolean isLeft) {
            expressionVisitor.setOriginalName(originalName);
            IExpression parseResult = expressionParser.parse(expression, expressionVisitor);
            if (!(parseResult instanceof IExpressionString)) {
                throw new IllegalArgumentException("Column name resolultion expression must evaluate to a string.");
            }
            return ((IExpressionString)parseResult).getString(0);
        }

        private class ExpressionVisitor extends ExpressionVisitorImpl {
            private IExpressionString originalName;

            public ExpressionVisitor(FunctionRegistry functionRegistry, Map<String, String> columnAliases) {
                super(null, functionRegistry, columnAliases, null, null);
            }

            @Override
            protected IExpression getColumnExpression(String columnName) {
                if (!"originalName".equals(columnName)) {
                    throw new IllegalArgumentException("No such column '" + columnName + "'");
                }
                return originalName;
            }

            public void setOriginalName(String originalName) {
                this.originalName = new LiteralString(originalName);
            }
        }
    }
}
