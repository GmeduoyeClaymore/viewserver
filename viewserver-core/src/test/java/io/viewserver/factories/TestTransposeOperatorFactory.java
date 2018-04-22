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
import io.viewserver.operators.transpose.ITransposeConfig;
import io.viewserver.operators.transpose.TransposeOperator;
import io.viewserver.schema.ITableStorage;

import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestTransposeOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;
    private ITableStorage tableStorage;

    public static String KEY_COLUMNS_PARAM_NAME = "keyColumns";
    public static String PIVOT_COLUMN_PARAM_NAME = "pivotColumns";
    public static String PIVOT_VALUES_PARAM_NAME = "pivotValues";


    public TestTransposeOperatorFactory(IExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.tableStorage = tableStorage;
    }

    @Override
    public String getOperatorType() {
        return "transpose";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        TransposeOperator transposeOperator = new TransposeOperator(operatorName, executionContext, catalog,tableStorage);
        configure(operatorName,context);
        return transposeOperator;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        IOperator operator = catalog.getOperatorByPath(operatorName);
        if(operator == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }
        List<String> sortDescriptors = ITestOperatorFactory.getParam(KEY_COLUMNS_PARAM_NAME, config,"List<String>()",false);
        String pivotColumn = ITestOperatorFactory.getParam(PIVOT_COLUMN_PARAM_NAME, config,String.class);
        List<Integer> pivotValues = ITestOperatorFactory.getParam(PIVOT_VALUES_PARAM_NAME, config,"List<Integer>()",false);
        ((TransposeOperator)operator).configure(new ITransposeConfig() {
            @Override
            public List<String> getKeyColumns() {
                return sortDescriptors;
            }

            @Override
            public String getPivotColumn() {
                return pivotColumn;
            }

            @Override
            public Object[] getPivotValues() {
                return pivotValues.toArray();
            }
        }, new CommandResult());
    }
}
