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
import io.viewserver.operators.sort.ISortConfig;
import io.viewserver.operators.sort.SortOperator;
import io.viewserver.schema.ITableStorage;

import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestSortOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;
    private ITableStorage tableStorage;

    public static String SORT_DESCRIPTORS_PARAM_NAME = "sortDescriptors";
    public static String START_PARAM_NAME = "start";
    public static String END_PARAM_NAME = "end";


    public TestSortOperatorFactory(IExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.tableStorage = tableStorage;
    }

    @Override
    public String getOperatorType() {
        return "sort";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        SortOperator filter = new SortOperator(operatorName, executionContext, catalog,tableStorage);
        configure(operatorName,context);
        return filter;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        IOperator operator = catalog.getOperatorByPath(operatorName);
        if(operator == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }

        String descriptor = ITestOperatorFactory.getParam(SORT_DESCRIPTORS_PARAM_NAME, config, String.class);
        Integer start = ITestOperatorFactory.getParam(START_PARAM_NAME, config, Integer.class);
        Integer end = ITestOperatorFactory.getParam(END_PARAM_NAME, config, Integer.class);

        String[] parts = descriptor.split("~");
        if(parts.length != 3){
            throw new RuntimeException(String.format("Invalid sort descriptor expecting the descriptor \"%s\"  to be in format RANK_COLUMN_NAME~SORT_COLUMN_NAME~DESCENDING",descriptor));
        }
        SortOperator.SortDescriptor sortDescriptor = new SortOperator.SortDescriptor(parts[0], parts[1], "desc".equals(parts[2].toLowerCase()));

        ((SortOperator)operator).configure(new ISortConfig() {
            @Override
            public SortOperator.SortDescriptor getSortDescriptor() {
                return sortDescriptor;
            }

            @Override
            public int getStart() {
                return start;
            }

            @Override
            public int getEnd() {
                return end;
            }
        }, new CommandResult());

    }
}
