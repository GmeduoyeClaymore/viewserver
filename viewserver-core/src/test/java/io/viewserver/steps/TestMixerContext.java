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

package io.viewserver.steps;

import io.viewserver.Constants;
import io.viewserver.catalog.Catalog;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.factories.*;
import io.viewserver.operators.*;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.index.IndexOperator;
import io.viewserver.operators.spread.SpreadFunctionRegistry;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.union.UnionOperator;
import io.viewserver.operators.validator.ValidationOperator;
import io.viewserver.operators.validator.ValidationUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 27/11/2014.
 */
public class TestMixerContext {
    private final Catalog catalog;
    private final IExecutionContext executionContext;
    private final FunctionRegistry functionRegistry;
    private final SummaryRegistry summaryRegistry;
    private final ChunkedColumnStorage tableStorage;
    private final SpreadFunctionRegistry spreadColumnRegistry;
    private HashMap<String,ITestOperatorFactory> operatorFactories;

    public TestMixerContext() {
        executionContext = new ExecutionContext();
        executionContext.setReactor(new TestReactor());
        functionRegistry = new FunctionRegistry();
        summaryRegistry = new SummaryRegistry();
        tableStorage = new ChunkedColumnStorage(1024);
        catalog = new Catalog(executionContext);
        operatorFactories = new HashMap<>();
        spreadColumnRegistry = new SpreadFunctionRegistry();
        ExecutionContext.blockThreadAssertion = true;

        register(new TestTableFactory(executionContext, catalog));
        register(new TestFilterOperatorFactory(executionContext, catalog,functionRegistry));
        register(new TestCalcColOperatorFactory(executionContext, catalog,functionRegistry));
        register(new TestGroupByOperatorFactory(executionContext, catalog,summaryRegistry,tableStorage));
        register(new TestIndexOperatorFactory(executionContext, catalog));
        register(new TestJoinOperatorFactory(executionContext, catalog));
        register(new TestSpreadOperatorFactory(executionContext, catalog, spreadColumnRegistry));
        register(new TestProjectionOperatorFactory(executionContext, catalog));
        register(new TestSortOperatorFactory(executionContext, catalog,tableStorage));
        register(new TestTransposeOperatorFactory(executionContext, catalog,tableStorage));
        register(new TestUnionOperatorFactory(executionContext, catalog,tableStorage));
    }
    public io.viewserver.operators.IOperator createOperator(String type, String name, HashMap<String, Object> context) {
        ITestOperatorFactory factory = getTestOperatorFactory(type);
        IOperator operator = factory.create(name, context);

        return operator;
    }

    private ITestOperatorFactory getTestOperatorFactory(String type) {
        ITestOperatorFactory factory = operatorFactories.get(type);
        if(factory == null){
            throw new RuntimeException("Unable to find factory for operator type " + type);
        }
        return factory;
    }

    public void configureChangeRecorderForOperator(String operatorName,String outputName){
        IOutput out = getOutput(operatorName, outputName);
        out.plugIn(new ChangeRecorder(operatorName + "_rec", executionContext, catalog).getInput());
    }
    public void pluginOperator(String outputOperator,String outputName,String inputOperator,String inputName){
        getOutput(outputOperator,outputName).plugIn(getInput(inputOperator, inputName));;
    }
    public void listenToOutput(String operatorName,String outputName,List<Map<String, String>> records) {
        String name = operatorName + "validator";
        ValidationOperator operator = (ValidationOperator) catalog.getOperator(name);
        if(operator == null) {
            operator = new ValidationOperator(name, executionContext, catalog);
            getOutput(operatorName, outputName).plugIn(operator.getInput(Constants.IN));

        }
        List<Object> validationActions = new ArrayList<>();
        for(Map<String,String> record : records){
            validationActions.add(ValidationUtils.to(record));
        }
        operator.setExpected(validationActions);
        operator.clearRecordedEvents();

    }
    private void register(ITestOperatorFactory testOperatorFactory) {
        operatorFactories.put(testOperatorFactory.getOperatorType(), testOperatorFactory);
    }
    private IOutput getOutput(String operatorName, String outputName) {
        IOperator operator = getOperator(operatorName);
        IOutput out = null;
        if(operator instanceof  IndexOperator){
            String[] indexOutputDescriptorParts = outputName.split("#");
            List<IndexOperator.QueryHolder> holders = new ArrayList<>();

            for(int i=0;i<indexOutputDescriptorParts.length;i++){
                String indexPartStr = indexOutputDescriptorParts[i];
                String[] indexParts = indexPartStr.split("~");
                if(indexParts.length != 2){
                    throw new RuntimeException(String.format("Invalid Index output descriptor \"%s\" should be in format COLUMN~INDEX_EXPRESSION " ,indexPartStr));
                }

                String[] split = indexParts[1].split(",");
                int[] indexValues = new int[split.length];

                for(int j=0;j<split.length;j++){
                    indexValues[j] = Integer.parseInt(split[j]);
                }
                holders.add(new IndexOperator.QueryHolder(indexParts[0], indexValues));
            }
            out = ((IndexOperator)operator).getOrCreateOutput(Constants.OUT,holders.toArray(new IndexOperator.QueryHolder[holders.size()]) );
        }else{
            out = operator.getOutput(outputName);
        }

        if(out == null){
            throw new RuntimeException("Operator named \"" + operatorName + "\" doesn't have an output named \"" + outputName + "\"");
        }
        return out;
    }

    public IOperator getOperator(String operatorName) {
        IOperator operator = catalog.getOperator(operatorName);
        if(operator == null){
            throw new RuntimeException("Unable to find operator named \"" + operatorName + "\" in catalog");
        }
        return operator;
    }

    private IInput getInput(String operatorName, String inputName) {
        IOperator operator = getOperator(operatorName);

        if(operator instanceof  UnionOperator){
            String[] parts = inputName.split("~");
            if(parts.length != 2){
                throw new RuntimeException(String.format("Invalid input name descriptor \"%s\" expecting format INPUT_NAME~SOURCE_ID",inputName));
            }
            return ((UnionOperator) operator).getOrCreateInput(parts[0],Integer.parseInt(parts[1]));
        }

        IInput input = operator.getInput(inputName);
        if(input == null){
            throw new RuntimeException("Operator named \"" + operatorName + "\" doesn't have an input named \"" + inputName + "\"");
        }
        return input;
    }

    public void commit() {
        executionContext.commit();
    }

    public void updateTable(String tableOperatorName, List<Map<String, String>> records) {
        TestTableFactory factory = (TestTableFactory) operatorFactories.get("table");
        factory.updateTable(tableOperatorName, records);
    }

    public void resetTable(String tableOperatorName) {
        ((KeyedTable)catalog.getOperator(tableOperatorName)).resetData();
    }

    public void configureOperator(String operatorType,String operatorName, HashMap<String, Object> context) {
        ITestOperatorFactory factory = getTestOperatorFactory(operatorType);
        factory.configure(operatorName,context);

    }
}
