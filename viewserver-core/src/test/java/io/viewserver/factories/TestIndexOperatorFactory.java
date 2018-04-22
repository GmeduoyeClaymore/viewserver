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
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.index.IIndexConfig;
import io.viewserver.operators.index.IndexOperator;
import io.viewserver.schema.ITableStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestIndexOperatorFactory implements ITestOperatorFactory{
    private IExecutionContext executionContext;
    private ICatalog catalog;
    private SummaryRegistry registry;
    private ITableStorage tableStorage;

    public static String INDEX_PARAM_NAME = "index";

    public TestIndexOperatorFactory(IExecutionContext executionContext, ICatalog catalog) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.registry = registry;
        this.tableStorage = tableStorage;
    }

    @Override
    public String getOperatorType() {
        return "index";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        IndexOperator index = new IndexOperator(operatorName, executionContext, catalog);
        configure(operatorName,context);
        return index;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){
        IndexOperator indexOperator = (IndexOperator)catalog.getOperatorByPath(operatorName);
        if(indexOperator == null){
            throw new RuntimeException("Unable to find operator named "  + operatorName + " in catalog");
        }
        List<String> indicies = new ArrayList<>();

        for(int i=0;i<10;i++){
            String indexParamName = ITestOperatorFactory.getParam(INDEX_PARAM_NAME + (i+1), config, String.class,true);
            if(indexParamName != null){
                indicies.add(indexParamName);
            }
        }
        indexOperator.configure(new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return indicies.toArray(new String[indicies.size()]);
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        }, new CommandResult());
    }


}
