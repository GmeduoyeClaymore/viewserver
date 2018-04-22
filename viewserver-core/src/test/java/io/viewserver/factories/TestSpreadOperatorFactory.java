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
import io.viewserver.operators.IOperator;
import io.viewserver.operators.spread.ISpreadFunctionRegistry;
import io.viewserver.operators.spread.ISpreadConfig;
import io.viewserver.operators.spread.SpreadOperator;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestSpreadOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;
    private ISpreadFunctionRegistry registry;

    public static String INPUT_COLUMN_PARAM_NAME = "inputColumn";
    public static String OUTPUT_COLUMN_PARAM_NAME = "outputColumn";
    public static String SPREAD_FUNCTION_PARAM_NAME = "spreadFunction";
    public static String REMOVE_INPUT_COLUMN_PARAM_NAME = "removeInputColumn";

    public TestSpreadOperatorFactory(IExecutionContext executionContext, ICatalog catalog, ISpreadFunctionRegistry registry) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.registry = registry;
    }

    @Override
    public String getOperatorType() {
        return "spread";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        SpreadOperator filter = new SpreadOperator(operatorName, executionContext,new ChunkedColumnStorage(1024), catalog, this.registry);
        configure(operatorName,context);
        return filter;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        IOperator spread = catalog.getOperatorByPath(operatorName);
        if(spread == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }
        String inputOperator =  ITestOperatorFactory.getParam(INPUT_COLUMN_PARAM_NAME, config, String.class);
        String outputOperator =  ITestOperatorFactory.getParam(OUTPUT_COLUMN_PARAM_NAME, config, String.class);
        String spreadFunction =  ITestOperatorFactory.getParam(SPREAD_FUNCTION_PARAM_NAME, config, String.class);
        boolean removeInputColumn =  ITestOperatorFactory.getParam(REMOVE_INPUT_COLUMN_PARAM_NAME, config, boolean.class);
        ((SpreadOperator) spread).configure(new ISpreadConfig() {
            @Override
            public String getInputColumnName() {
                return inputOperator;
            }

            @Override
            public String getOutputColumnName() {
                return outputOperator;
            }

            @Override
            public String spreadFunctionName() {
                return spreadFunction;
            }

            @Override
            public boolean removeInputColumn() {
                return removeInputColumn;
            }
        }, new CommandResult());
    }


}
