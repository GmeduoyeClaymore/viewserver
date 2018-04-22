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
import io.viewserver.operators.group.GroupByOperator;
import io.viewserver.operators.group.IGroupByConfig;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.schema.ITableStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestGroupByOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;
    private SummaryRegistry registry;
    private ITableStorage tableStorage;

    public static String GROUP_BY_PARAM_NAME = "groupBy";
    public static String SUMMARY_NAME_PARAM_NAME = "summaryName";
    public static String SUMMARY_FUNCTION_PARAM_NAME = "summaryFunction";
    public static String SUMMARY_ARGUMENT_PARAM_NAME = "summaryArgument";

    public TestGroupByOperatorFactory(IExecutionContext  executionContext, ICatalog catalog, SummaryRegistry registry,ITableStorage tableStorage) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.registry = registry;
        this.tableStorage = tableStorage;
    }

    @Override
    public String getOperatorType() {
        return "groupBy";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        GroupByOperator groupByOperator = new GroupByOperator(operatorName, executionContext, catalog, registry,tableStorage);
        configure(operatorName,context);
        return groupByOperator;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        GroupByOperator group = (GroupByOperator)catalog.getOperatorByPath(operatorName);
        if(group == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }
        List<String> groupBy = new ArrayList<>();
        List<IGroupByConfig.Summary> summaries = new ArrayList<>();
        for(int i=0;i<10;i++){
            String groupByParamName = ITestOperatorFactory.getParam(GROUP_BY_PARAM_NAME + (i+1), config, String.class,true);
            String summaryName = ITestOperatorFactory.getParam(SUMMARY_NAME_PARAM_NAME + (i+1), config, String.class,true);
            String summaryFunction = ITestOperatorFactory.getParam(SUMMARY_FUNCTION_PARAM_NAME + (i+1), config, String.class,true);
            String summaryArgument = ITestOperatorFactory.getParam(SUMMARY_ARGUMENT_PARAM_NAME + (i+1), config, String.class,true);
            if(groupByParamName != null){
                groupBy.add(groupByParamName);
            }
            if(summaryName != null && summaryFunction != null&& summaryArgument != null){
                summaries.add(new IGroupByConfig.Summary(summaryName, summaryFunction, summaryArgument));
            }
        }
        group.configure(new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                return groupBy;
            }

            @Override
            public List<Summary> getSummaries() {
                return summaries;
            }

            @Override
            public String getCountColumnName() {
                return null;
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

        }, new CommandResult());
    }


}
