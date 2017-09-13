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

import io.viewserver.core.Hasher;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.projection.IProjectionConfig;
import com.facebook.presto.sql.tree.*;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by nick on 19/11/15.
 */
public class SqlSelectStep implements IExecutionPlanStep<SqlExecutionPlanContext> {
    private SummaryRegistry summaryRegistry;

    public SqlSelectStep(SummaryRegistry summaryRegistry) {
        this.summaryRegistry = summaryRegistry;
    }

    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        final Query query = executionPlanContext.getQuery();
        final QuerySpecification body = (QuerySpecification) query.getQueryBody();
        final Select select = body.getSelect();
        if (select.isDistinct()) {
            throw new UnsupportedOperationException("Distinct queries are not implemented");
        }
        final List<SelectItem> selectItems = select.getSelectItems();
        final int count = selectItems.size();
        final GroupByNode groupBy = executionPlanContext.getGroupBy();
        if (groupBy == null && count == 1 && selectItems.get(0) instanceof AllColumns && !((AllColumns) selectItems.get(0)).getPrefix().isPresent()) {
            return;
        }
        for (int i = 0; i < count; i++) {
            final SelectItem selectItem = selectItems.get(i);
            if (selectItem instanceof AllColumns) {
                if (groupBy != null) {
                    throw new UnsupportedOperationException("Asterisks are not supported in GROUP BY queries");
                }
                String prefix = ((AllColumns) selectItem).getPrefix().get().toString();
                getProjection(executionPlanContext).withProjectionColumns(new IProjectionConfig.ProjectionColumn(
                        "^" + prefix + "\\.(.+)$", true, "$1"));
            } else if (selectItem instanceof SingleColumn) {
                final SingleColumn singleColumn = (SingleColumn) selectItem;
                final String alias;
                if (singleColumn.getAlias().isPresent()) {
                    alias = singleColumn.getAlias().get();
                } else {
                    alias = null;
                }

                Expression expression = singleColumn.getExpression();
                if (expression instanceof QualifiedNameReference) {
                    if (executionPlanContext.isUsingPrefixes()) {
                        throw new UnsupportedOperationException("If using table alias prefixes, they must be used on all columns.");
                    }
                    String columnName = ((QualifiedNameReference) expression).getName().getOriginalParts().get(0);
                    getProjection(executionPlanContext).withProjectionColumns(new IProjectionConfig.ProjectionColumn(columnName, alias));
                } else if (expression instanceof DereferenceExpression) {
                    DereferenceExpression dereferenceExpression = (DereferenceExpression) expression;
                    Expression base = dereferenceExpression.getBase();
                    if (base instanceof QualifiedNameReference) {
                        getProjection(executionPlanContext).withProjectionColumns(new IProjectionConfig.ProjectionColumn(
                                ((QualifiedNameReference) base).getName().toString() + "." + dereferenceExpression.getFieldName(),
                                alias != null ? alias : dereferenceExpression.getFieldName()
                        ));
                    } else {
                        throw new UnsupportedOperationException(String.format("Unhandled base expression type (%s) for %s",
                                base.getClass(), base));
                    }
                } else {
                    final SelectExpressionRewriteContext context = new SelectExpressionRewriteContext();
                    final SelectExpressionRewriter rewriter = new SelectExpressionRewriter(executionPlanContext);
                    expression = ExpressionTreeRewriter.rewriteWith(rewriter, expression, context);

                    if (expression instanceof QualifiedNameReference) {
                        String columnName = ((QualifiedNameReference) expression).getSuffix().toString();
                        getProjection(executionPlanContext).withProjectionColumns(new IProjectionConfig.ProjectionColumn(columnName, alias));
                    } else if (expression instanceof DereferenceExpression) {
                        DereferenceExpression dereferenceExpression = (DereferenceExpression) expression;
                        Expression base = dereferenceExpression.getBase();
                        if (base instanceof QualifiedNameReference) {
                            getProjection(executionPlanContext).withProjectionColumns(new IProjectionConfig.ProjectionColumn(
                                    ((QualifiedNameReference) base).getName().toString() + "." + dereferenceExpression.getFieldName(),
                                    alias != null ? alias : dereferenceExpression.getFieldName()
                            ));
                        } else {
                            throw new UnsupportedOperationException(String.format("Unhandled base expression type (%s) for %s",
                                    base.getClass(), base));
                        }
                    } else {
                        final CalcColNode calculations = context.aggregating
                                ? executionPlanContext.getOrCreatePostSummaryCalculations()
                                : executionPlanContext.getOrCreatePreSummaryCalculations();
                        final String calculationName = alias != null ? alias : UUID.randomUUID().toString();
                        final String calculationExpression = SqlExpressionFormatter.formatExpression(expression);
                        calculations.withCalculations(new CalcColOperator.CalculatedColumn(calculationName, calculationExpression));
                        if (groupBy == null || context.aggregating) {
                            getProjection(executionPlanContext).withProjectionColumns(new IProjectionConfig.ProjectionColumn(calculationName));
                        }
                    }
                }
            } else {
                throw new UnsupportedOperationException(String.format("Unknown type of selectItem (%s)", selectItem.getClass()));
            }
        }
    }

    private ProjectionNode getProjection(SqlExecutionPlanContext executionPlanContext) {
        ProjectionNode projection = executionPlanContext.getProjection();
        if (projection == null) {
            projection = new ProjectionNode("projection")
                    .withMode(IProjectionConfig.ProjectionMode.Inclusionary);
            executionPlanContext.setProjection(projection);
            executionPlanContext.getGraphNodes().add(projection);
        }
        return projection;
    }

    private class SelectExpressionRewriter extends SupportedExpressionRewriter<SelectExpressionRewriteContext> {
        private SqlExecutionPlanContext executionPlanContext;

        public SelectExpressionRewriter(SqlExecutionPlanContext executionPlanContext) {
            this.executionPlanContext = executionPlanContext;
        }

        @Override
        public Expression rewriteExpression(Expression node, SelectExpressionRewriteContext context, ExpressionTreeRewriter<SelectExpressionRewriteContext> treeRewriter) {
            if (context.insideSummaryFunctionCall) {
                context.insideSummaryFunctionCall = false;
                final CalcColNode preSummaryCalculations = executionPlanContext.getOrCreatePreSummaryCalculations();
                final String calculationExpression = SqlExpressionFormatter.formatExpression(node);
                final String calculationName = String.format("calc_%s", Hasher.SHA1(calculationExpression));
                preSummaryCalculations.withCalculations(new CalcColOperator.CalculatedColumn(calculationName, calculationExpression));
                return new QualifiedNameReference(QualifiedName.of(calculationName));
            }
            return null;
        }

        @Override
        public Expression rewriteQualifiedNameReference(QualifiedNameReference node, SelectExpressionRewriteContext context, ExpressionTreeRewriter<SelectExpressionRewriteContext> treeRewriter) {
            return null;
        }

        @Override
        public Expression rewriteDereferenceExpression(DereferenceExpression node, SelectExpressionRewriteContext context, ExpressionTreeRewriter<SelectExpressionRewriteContext> treeRewriter) {
            return null;
        }

        @Override
        public Expression rewriteFunctionCall(FunctionCall node, SelectExpressionRewriteContext context, ExpressionTreeRewriter<SelectExpressionRewriteContext> treeRewriter) {
            final String functionName = node.getName().toString();
            if (summaryRegistry.getSummaryFactory(functionName) != null) {
                if (context.insideSummaryFunctionCall) {
                    throw new UnsupportedOperationException("Cannot have nested summary functions");
                }
                context.insideSummaryFunctionCall = true;
                context.aggregating = true;
                final Expression target = node.getArguments().get(0);
                final Expression rewrittenTarget = treeRewriter.rewrite(target, context);
                final List<Expression> arguments;
                if (rewrittenTarget != target) {
                    arguments = new ArrayList<>(node.getArguments());
                    arguments.set(0, rewrittenTarget);
                } else {
                    arguments = node.getArguments();
                }

                List<Object> summaryArguments = new ArrayList<>();
                int argumentCount = arguments.size();
                for (int j = 1; j < argumentCount; j++) {
                    final Expression argument = arguments.get(j);
                    if (argument instanceof QualifiedNameReference) {
                        if (executionPlanContext.isUsingPrefixes()) {
                            throw new UnsupportedOperationException("If using table alias prefixes, they must be used on all columns.");
                        }
                        summaryArguments.add(((QualifiedNameReference) argument).getName().getOriginalParts().get(0));
                    } else if (argument instanceof DereferenceExpression) {
                        final DereferenceExpression dereferenceExpression = (DereferenceExpression) argument;
                        final Expression base = dereferenceExpression.getBase();
                        if (base instanceof QualifiedNameReference) {
                            summaryArguments.add(((QualifiedNameReference) base).getName().toString() + "." + dereferenceExpression.getFieldName());
                        } else {
                            throw new UnsupportedOperationException(String.format("Unhandled base expression type (%s) for %s",
                                    base.getClass(), base));
                        }
                    } else if (argument instanceof Literal) {
                        if (argument instanceof BooleanLiteral) {
                            summaryArguments.add(((BooleanLiteral) argument).getValue());
                        } else if (argument instanceof DoubleLiteral) {
                            summaryArguments.add(((DoubleLiteral) argument).getValue());
                        } else if (argument instanceof LongLiteral) {
                            summaryArguments.add(((LongLiteral) argument).getValue());
                        } else if (argument instanceof NullLiteral) {
                            summaryArguments.add(null);
                        } else if (argument instanceof StringLiteral) {
                            summaryArguments.add(((StringLiteral) argument).getValue());
                        } else if (argument instanceof TimestampLiteral) {
                            summaryArguments.add(DateTime.parse(((TimestampLiteral) argument).getValue()).toDate().getTime());
                        } else {
                            throw new UnsupportedOperationException(String.format("Unhandled literal type (%s) for %s",
                                    argument.getClass(), argument));
                        }
                    } else {
                        throw new UnsupportedOperationException(String.format("Unsupported expression type (%s) for summary argument %s",
                                argument.getClass(), argument));
                    }
                }
                String summaryName = String.format("summary_%s", Hasher.SHA1(SqlExpressionFormatter.formatExpression(new FunctionCall(node.getName(), node.getWindow(), node.isDistinct(), arguments))));
                executionPlanContext.getOrCreateGroupBy().withSummary(summaryName, functionName,
                        SqlExpressionFormatter.formatExpression(rewrittenTarget), summaryArguments.toArray());

                return new QualifiedNameReference(QualifiedName.of(summaryName));
            }

            return null;
        }
    }

    private class SelectExpressionRewriteContext {
        private boolean insideSummaryFunctionCall;
        private boolean aggregating;
    }
}
