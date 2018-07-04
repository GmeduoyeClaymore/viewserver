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
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.join.IColumnNameResolver;
import com.facebook.presto.sql.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by bemm on 19/11/15.
 */
public class SqlFromStep implements IExecutionPlanStep<SqlExecutionPlanContext> {
    private boolean firstJoin = true;
    private SummaryRegistry summaryRegistry;
    private IExecutionPlanRunner executionPlanRunner;

    public SqlFromStep(SummaryRegistry summaryRegistry, IExecutionPlanRunner executionPlanRunner) {
        this.summaryRegistry = summaryRegistry;
        this.executionPlanRunner = executionPlanRunner;
    }

    @Override
    public void execute(SqlExecutionPlanContext executionPlanContext) {
        Query query = executionPlanContext.getQuery();
        QuerySpecification body = (QuerySpecification) query.getQueryBody();
        Relation from = body.getFrom().get();
        addTablesFrom(from, executionPlanContext);
    }

    private void addTablesFrom(Relation relation, SqlExecutionPlanContext executionPlanContext) {
        if (relation instanceof Table) {
            addTable((Table) relation, executionPlanContext);
        } else if (relation instanceof TableSubquery) {
            addOperator(getOperatorNameForTableSubquery((TableSubquery)relation, executionPlanContext), executionPlanContext);
        } else if (relation instanceof AliasedRelation) {
            addTablesFrom(((AliasedRelation) relation).getRelation(), executionPlanContext);
        } else if (relation instanceof Join) {
            addTablesFrom(((Join) relation).getLeft(), executionPlanContext);
            addJoin((Join) relation, executionPlanContext);
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported relation type (%s)", relation.getClass()));
        }
    }

    private void addTable(Table table, SqlExecutionPlanContext executionPlanContext) {
        OperatorName name = getOperatorNameForTable(table);

        addOperator(name, executionPlanContext);
    }

    private void addOperator(OperatorName name, SqlExecutionPlanContext executionPlanContext) {
        if (executionPlanContext.getInputOperator() == null) {
            executionPlanContext.setInput(name.operator, name.output);
        }
    }

    private void addJoin(Join join, SqlExecutionPlanContext executionPlanContext) {
        Relation right = join.getRight();
        final String joinName = String.format("join-%s", right);
        final JoinNode joinNode = new JoinNode(joinName)
                .withConnection(executionPlanContext.getInputOperator(), executionPlanContext.getInputOutputName(), "left");
        final OperatorName rightName;
        String rightAlias;
        if (right instanceof Table) {
            rightName = getOperatorNameForTable((Table) right);
            rightAlias = rightName.operator;
        } else if (right instanceof AliasedRelation) {
            AliasedRelation aliasedRelation = (AliasedRelation) right;
            rightAlias = aliasedRelation.getAlias();
            right = aliasedRelation.getRelation();
            if (right instanceof Table) {
                rightName = getOperatorNameForTable((Table) right);
            } else if (right instanceof TableSubquery) {
                rightName = getOperatorNameForTableSubquery((TableSubquery)right, executionPlanContext);
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported type (%s) for \"%s\" on right side of join",
                        right.getClass(), right));
            }
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported type (%s) for \"%s\" on right side of join",
                    right.getClass(), right));
        }
        joinNode.withConnection(rightName.operator, rightName.output, "right");
        switch (join.getType()) {
            case LEFT: {
                joinNode.withLeftJoinOuter();
                break;
            }
            case RIGHT: {
                joinNode.withRightJoinOuter();
                break;
            }
            case FULL: {
                joinNode.withLeftJoinOuter().withRightJoinOuter();
                break;
            }
        }
        Optional<JoinCriteria> criteria = join.getCriteria();
        if (criteria.isPresent()) {
            if (!(criteria.get() instanceof JoinOn)) {
                throw new UnsupportedOperationException(String.format("Unsupported join criteria type (%s) for \"%s\"",
                        criteria.get().getClass(), criteria.get()));
            }
            Expression joinExpression = ((JoinOn) criteria.get()).getExpression();
            List<String> leftJoinColumns = new ArrayList<>();
            List<String> rightJoinColumns = new ArrayList<>();
            getJoinColumns(joinExpression, right, leftJoinColumns, rightJoinColumns);
            if (!leftJoinColumns.isEmpty()) {
                joinNode.withLeftJoinColumns(leftJoinColumns);
            }
            if (!rightJoinColumns.isEmpty()) {
                joinNode.withRightJoinColumns(rightJoinColumns);
            }
        }
        if (executionPlanContext.isUsingPrefixes()) {
            joinNode.withColumnNameResolver(new JoinColumnNamer(firstJoin ? executionPlanContext.getPrefixForTableName(
                    executionPlanContext.getInputOperator()) + "." : "", rightAlias + "."))
                    .withAlwaysResolveNames();
        }
        executionPlanContext.getGraphNodes().add(joinNode);
        executionPlanContext.setInput(joinNode.getName());

        firstJoin = false;
    }

    private OperatorName getOperatorNameForTableSubquery(TableSubquery subquery, SqlExecutionPlanContext executionPlanContext) {
        final CommandResult sqlExecutionPlanResult = new CommandResult("SQL table subquery");

        final boolean permanent = executionPlanContext.isPermanent();
        final SqlExecutionPlanContext context = new SqlExecutionPlanContext(subquery.getQuery(), permanent);

        final SqlExecutionPlan executionPlan = new SqlExecutionPlan(summaryRegistry, executionPlanRunner);
        final ICatalog catalog = executionPlanContext.getCatalog();
        executionPlanRunner.executePlan(executionPlan, context, executionPlanContext.getExecutionContext(),
                catalog, sqlExecutionPlanResult);

        return new OperatorName(context.getInputOperator(), context.getInputOutputName());
    }

    private void getJoinColumns(Expression expression, Relation right, List<String> leftJoinColumns, List<String> rightJoinColumns) {
        if (expression instanceof ComparisonExpression) {
            ComparisonExpression comparisonExpression = (ComparisonExpression) expression;
            if (comparisonExpression.getType() == ComparisonExpression.Type.EQUAL) {
                String[] leftColumnName = getColumnNameFromExpression(comparisonExpression.getLeft());
                String[] rightColumnName = getColumnNameFromExpression(comparisonExpression.getRight());
                leftJoinColumns.add(leftColumnName[1]);
                rightJoinColumns.add(rightColumnName[1]);
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported comparison type (%s) in join criteria for \"%s\"",
                        comparisonExpression.getType(), expression));
            }
        } else if (expression instanceof LogicalBinaryExpression) {
            LogicalBinaryExpression logicalBinaryExpression = (LogicalBinaryExpression) expression;
            if (logicalBinaryExpression.getType() == LogicalBinaryExpression.Type.AND) {
                getJoinColumns(logicalBinaryExpression.getLeft(), right, leftJoinColumns, rightJoinColumns);
                getJoinColumns(logicalBinaryExpression.getRight(), right, leftJoinColumns, rightJoinColumns);
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported binary expression type (%s) in join criteria for \"%s\"",
                        logicalBinaryExpression.getType(), expression));
            }
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported expression type (%s) in join criteria for \"%s\"",
                    expression.getClass(), expression));
        }
    }

    private String[] getColumnNameFromExpression(Expression expression) {
        if (expression instanceof QualifiedNameReference) {
            return new String[]{null, ((QualifiedNameReference) expression).getName().toString()};
        } else if (expression instanceof DereferenceExpression) {
            DereferenceExpression dereferenceExpression = (DereferenceExpression) expression;
            String[] nameForBaseExpression = getColumnNameFromExpression(dereferenceExpression.getBase());
            return new String[]{nameForBaseExpression[1], dereferenceExpression.getFieldName()};
        } else {
            throw new UnsupportedOperationException(String.format("Unhandled expression type (%s) for column name in join criteria \"%s\"",
                    expression.getClass(), expression));
        }
    }

    private OperatorName getOperatorNameForTable(Table table) {
        final String operator;
        final String output;
        final Optional<QualifiedName> prefix = table.getName().getPrefix();
        if (prefix.isPresent()) {
            operator = prefix.get().getOriginalParts().get(0);
            output = table.getName().getSuffix();
        } else {
            operator = table.getName().getOriginalParts().get(0);
            output = Constants.OUT;
        }
        return new OperatorName(operator, output);
    }

    private class OperatorName {
        private String operator;
        private String output;

        public OperatorName(String operator, String output) {
            this.operator = operator;
            this.output = output;
        }
    }

    private class JoinColumnNamer implements IColumnNameResolver {
        private final String leftPrefix;
        private final String rightPrefix;

        public JoinColumnNamer(String leftPrefix, String rightPrefix) {
            this.leftPrefix = leftPrefix;
            this.rightPrefix = rightPrefix;
        }

        @Override
        public String resolveColumnName(String originalName, boolean isLeft) {
            return String.format("%s%s", isLeft ? leftPrefix : rightPrefix, originalName);
        }
    }
}
