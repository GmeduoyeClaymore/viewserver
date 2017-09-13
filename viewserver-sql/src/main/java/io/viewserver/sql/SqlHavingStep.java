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
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.group.summary.SummaryRegistry;
import com.facebook.presto.sql.tree.*;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by nick on 20/11/15.
 */
public class SqlHavingStep extends SqlFilterStepBase {
    private SummaryRegistry summaryRegistry;

    public SqlHavingStep(SummaryRegistry summaryRegistry) {
        this.summaryRegistry = summaryRegistry;
    }

    @Override
    protected String getName() {
        return "having";
    }

    @Override
    protected Optional<Expression> getFilterExpression(QuerySpecification body, SqlExecutionPlanContext executionPlanContext) {
        Optional<Expression> having = body.getHaving();
        if (having.isPresent()) {
            if (executionPlanContext.getGroupBy() == null) {
                throw new UnsupportedOperationException("Cannot have HAVING clause without a GROUP BY clause");
            }
            final HavingExpressionRewriteContext context = new HavingExpressionRewriteContext();
            final HavingExpressionRewriter rewriter = new HavingExpressionRewriter(executionPlanContext);
            having = Optional.of(ExpressionTreeRewriter.rewriteWith(rewriter, having.get(), context));
        }
        return having;
    }

    @Override
    protected void updateContext(SqlExecutionPlanContext executionPlanContext, FilterNode filter) {
        executionPlanContext.setHaving(filter);
    }

    private class HavingExpressionRewriter extends SupportedExpressionRewriter<HavingExpressionRewriteContext> {
        private SqlExecutionPlanContext executionPlanContext;

        public HavingExpressionRewriter(SqlExecutionPlanContext executionPlanContext) {
            this.executionPlanContext = executionPlanContext;
        }

        @Override
        public Expression rewriteExpression(Expression node, HavingExpressionRewriteContext context, ExpressionTreeRewriter<HavingExpressionRewriteContext> treeRewriter) {
            if (context.rewritingSummaryTarget) {
                context.rewritingSummaryTarget = false;
                final CalcColNode preSummaryCalculations = executionPlanContext.getOrCreatePreSummaryCalculations();
                final String calculationExpression = SqlExpressionFormatter.formatExpression(node);
                final String calculationName = String.format("calc_%s", Hasher.SHA1(calculationExpression));
                preSummaryCalculations.withCalculations(new CalcColOperator.CalculatedColumn(calculationName, calculationExpression));
                return new QualifiedNameReference(QualifiedName.of(calculationName));
            }
            return null;
        }

        @Override
        public Expression rewriteQualifiedNameReference(QualifiedNameReference node, HavingExpressionRewriteContext context, ExpressionTreeRewriter<HavingExpressionRewriteContext> treeRewriter) {
            if (!context.rewritingSummaryTarget) {
                throw new UnsupportedOperationException("HAVING clause cannot reference unaggregated columns");
            }
            return null;
        }

        @Override
        public Expression rewriteDereferenceExpression(DereferenceExpression node, HavingExpressionRewriteContext context, ExpressionTreeRewriter<HavingExpressionRewriteContext> treeRewriter) {
            if (!context.rewritingSummaryTarget) {
                throw new UnsupportedOperationException("HAVING clause cannot reference unaggregated columns");
            }
            return null;
        }

        @Override
        public Expression rewriteFunctionCall(FunctionCall node, HavingExpressionRewriteContext context, ExpressionTreeRewriter<HavingExpressionRewriteContext> treeRewriter) {
            final String functionName = node.getName().toString();
            if (summaryRegistry.getSummaryFactory(functionName) != null) {
                if (context.rewritingSummaryTarget) {
                    throw new UnsupportedOperationException("Cannot have nested summary functions");
                }
                context.rewritingSummaryTarget = true;
                context.aggregating = true;
                final Expression target = node.getArguments().get(0);
                final Expression rewrittenTarget = treeRewriter.rewrite(target, context);
                context.rewritingSummaryTarget = false;
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

    private class HavingExpressionRewriteContext {
        private boolean rewritingSummaryTarget;
        private boolean aggregating;
    }
}
