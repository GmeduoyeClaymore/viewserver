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

import io.viewserver.core.Hasher;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.expression.AntlrExpressionParser;
import io.viewserver.expression.parser.ColumnAliasingVisitor;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.report.CalculationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 31/10/2014.
 */
public abstract class CalculationStepBase<TGraphDefinition> implements IExecutionPlanStep<ReportExecutionPlanContext> {
    private static final Logger logger = LoggerFactory.getLogger(CalculationStepBase.class);
    @Override
    public void execute(final ReportExecutionPlanContext context) {
        ReportContext reportContext = context.getReportContext();

        try {
            TGraphDefinition graphDefinition = (TGraphDefinition) context.getGraphDefinition();

            ParameterHelper parameterHelper = context.getParameterHelper();
            for (CalculationDefinition calculationDefinition : getCalculations(reportContext, graphDefinition)) {
                String expression = parameterHelper.substituteParameterValues(calculationDefinition.getExpression(), reportContext);
                expression = updateColumnNames(expression, context.getCalculationAliases());
                String columnName = "calc_" + Hasher.SHA1(expression);
                context.getCalculationAliases().put(parameterHelper.substituteParameterValues(calculationDefinition.getName()), columnName);
                context.getCalculations().add(new CalcColOperator.CalculatedColumn(columnName, expression));
            }
        }catch (Exception ex){
            logger.warn("Problem running calculation step {}", ex.getMessage());
        }
    }

    protected abstract List<CalculationDefinition> getCalculations(ReportContext reportContext, TGraphDefinition graphDefinition);

    private String updateColumnNames(String expression, Map<String, String> aliases) {
        return new AntlrExpressionParser().parse(expression, new ColumnAliasingVisitor(aliases));
    }
}
