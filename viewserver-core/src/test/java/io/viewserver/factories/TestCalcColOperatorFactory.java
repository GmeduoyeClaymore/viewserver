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

package io.viewserver.factories;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.calccol.ICalcColConfig;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestCalcColOperatorFactory implements ITestOperatorFactory{

    private IExecutionContext executionContext;
    private ICatalog catalog;
    private FunctionRegistry registry;


    public static String CALC_COLUMN_NAME = "calcColumnName";
    public static String CALC_COLUMN_REGEX = "calcColumnRegex";
    public static String CALC_EXPRESSION_PARAM_NAME = "calcExpression";

    public TestCalcColOperatorFactory(IExecutionContext executionContext, ICatalog catalog, FunctionRegistry registry) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.registry = registry;
    }

    @Override
    public String getOperatorType() {
        return "calc";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        CalcColOperator calc = new CalcColOperator(operatorName, executionContext, catalog, new ChunkedColumnStorage(1024), executionContext.getExpressionParser());
        configure(operatorName,context);
        return calc;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        CalcColOperator calc = (CalcColOperator) catalog.getOperatorByPath(operatorName);
        if(calc == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }
        List<CalcColOperator.CalculatedColumn> result = new ArrayList<>();
        for(int i=0;i<10;i++){
            String calculatorExpression = ITestOperatorFactory.getParam(CALC_EXPRESSION_PARAM_NAME + (i+1), config, String.class,true);
            String calcColumnName = ITestOperatorFactory.getParam(CALC_COLUMN_NAME + (i+1), config, String.class,true);
            String calcColumnRegex = ITestOperatorFactory.getParam(CALC_COLUMN_REGEX + (i+1), config, String.class,true);
            if(calculatorExpression != null && calcColumnName != null){
                result.add(new CalcColOperator.CalculatedColumn(calcColumnName, calculatorExpression, calcColumnRegex));
            }
        }

        calc.configure(new ICalcColConfig() {
            @Override
            public List<CalcColOperator.CalculatedColumn> getCalculatedColumns() {
                return result;
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }

            @Override
            public boolean isDataRefreshedOnColumnAdd() {
                return true;
            }
        },new CommandResult());
    }


}
