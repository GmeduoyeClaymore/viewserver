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
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.operators.filter.IFilterConfig;

import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestFilterOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;
    private FunctionRegistry registry;

    public static String FILTER_MODE_PARAM_NAME = "filterMode";
    public static String FILTER_EXPRESSION_PARAM_NAME = "filterExpression";

    public TestFilterOperatorFactory( IExecutionContext executionContext, ICatalog catalog,FunctionRegistry registry) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.registry = registry;
    }

    @Override
    public String getOperatorType() {
        return "filter";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        FilterOperator filter = new FilterOperator(operatorName, executionContext, catalog, executionContext.getExpressionParser());
        configure(operatorName,context);
        return filter;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        IOperator filter = catalog.getOperator(operatorName);
        if(filter == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }
        FilterOperator.FilterMode mode = ITestOperatorFactory.getParam(FILTER_MODE_PARAM_NAME, config, FilterOperator.FilterMode.class);
        String  filterExpression = ITestOperatorFactory.getParam(FILTER_EXPRESSION_PARAM_NAME, config, String.class);
        ((FilterOperator) filter).configure(new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return mode;
            }

            @Override
            public String getExpression() {
                return filterExpression;
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        }, new CommandResult());
    }


}
