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

import io.viewserver.factories.ITestOperatorFactory;
import io.viewserver.factories.TestTableFactory;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.validator.ValidationUtils;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.util.*;

/**
 * Created by bemm on 27/11/2014.
 */
public class OperatorSteps {


    private TestMixerContext testMixerContext;

    public OperatorSteps(TestMixerContext context) {
        ITestOperatorFactory.register(param -> {
            return FilterOperator.FilterMode.valueOf(param);
        }, FilterOperator.FilterMode.class);
        ITestOperatorFactory.register(param -> {
            return Integer.parseInt(param);
        }, Integer.class);
        ITestOperatorFactory.register(param -> {
            List<String> result = new ArrayList<String>();
            if(param == null){
                return result;
            }
            return Arrays.asList(param.split(","));
        }, "List<String>()");
        ITestOperatorFactory.register(param -> {
            List<Integer> result = new ArrayList<Integer>();
            if(param != null){
                for(String value : param.split(",")){
                    result.add(Integer.parseInt(value));
                }
            }
            return result;
        }, "List<Integer>()");
        this.testMixerContext = context;
    }

    @Given("^table named \"([^\"]*)\" with data$")
    public void table_created_containing_data(String tableName,List<Map<String,String>> records) throws Throwable {
        HashMap<String,Object> context = new HashMap<>();
        context.put(TestTableFactory.RECORDS_PARAM_NAME,new ArrayList<>(records));
        testMixerContext.createOperator("table",tableName,context);
    }
    @Then("^operator \"([^\"]*)\" output \"([^\"]*)\" is$")
    public void output_on_operator_contains(String operatorName,String outputName,List<Map<String,String>> records) throws Throwable {
        testMixerContext.listenToOutput(operatorName, outputName, records);
    }
    @Then("^commit$")
    public void output_on_operator_contains() throws Throwable {
        testMixerContext.commit();
    }

    @Given("^operator type \"([^\"]*)\" named \"([^\"]*)\" created$")
    public void operator(String type,String name,List<Map<String,String>> context) throws Throwable {
        HashMap<String, Object> result = getStringObjectHashMap(context);
        testMixerContext.createOperator(type, name, result);
    }

    @Given("^operator \"([^\"]*)\" output \"([^\"]*)\" plugged into \"([^\"]*)\" input \"([^\"]*)\"")
    public void operator_output_plugged_into_input(String outputOperatorName,String outputName,String inputOperator,String inputName) throws Throwable {
        testMixerContext.pluginOperator(outputOperatorName, outputName, inputOperator, inputName);
    }

    @And("^listen for changes on \"([^\"]*)\" output \"([^\"]*)\"$")
    public void listen_for_changes_on_output(String operator, String output) throws Throwable {
        testMixerContext.configureChangeRecorderForOperator(operator, output);
    }



    @When("^table \"([^\"]*)\" updated to$")
    public void rows_from_table_updated_to(String tableOperatorName, List<Map<String,String>> records) throws Throwable {
        testMixerContext.updateTable(tableOperatorName,records);
    }
    @When("^table \"([^\"]*)\" reset$")
    public void table_reset(String tableOperatorName) throws Throwable {
        testMixerContext.resetTable(tableOperatorName);
    }

    @Then("^operator \"([^\"]*)\" of type \"([^\"]*)\" is configured to$")
    public void operator_is_configured_to(String operatorName, String operatorType,List<Map<String,String>> context) throws Throwable {
        testMixerContext.configureOperator(operatorType,operatorName,getStringObjectHashMap(context));
    }

    private HashMap<String, Object> getStringObjectHashMap(List<Map<String, String>> context) {
        HashMap<String,Object> result = new HashMap<>();
        for(Map<String,String> row : context){
            String key = row.get("field");
            String value = row.get("value");
            result.put(key,value);
        }
        return result;
    }

    @When("^rows \"([^\"]*)\" removed from table \"([^\"]*)\"$")
    public void rows_removed_from_table(List rows, String tableOperatorName) throws Throwable {
        KeyedTable table = (KeyedTable)testMixerContext.getOperator(tableOperatorName);
        for(Object key : rows){
            table.removeRow(new TableKey(key));
        }
    }

    @When("^columns \"([^\"]*)\" removed from table \"([^\"]*)\"$")
    public void columns_removed_from_table(List<String> columns, String tableOperatorName) throws Throwable {
        KeyedTable table = (KeyedTable)testMixerContext.getOperator(tableOperatorName);
        Schema schema = table.getOutput().getSchema();
        for(String column : columns){
            schema.removeColumn(schema.getColumnHolder(column).getColumnId());
        }
    }

    @When("^columns added to table \"([^\"]*)\"$")
    public void columns_added_to_table(String tableName,List<Map<String, String>> context) throws Throwable {
        KeyedTable table = (KeyedTable)testMixerContext.getOperator(tableName);
        Schema schema = table.getOutput().getSchema();
        for(ColumnHolder holder : getColumnDefinitions(context)){
            schema.addColumn(holder);
            table.getStorage().initialiseColumn(holder);
        }
    }

    private List<ColumnHolder> getColumnDefinitions(List<Map<String, String>> context) {
        List<ColumnHolder> result = new ArrayList<>();
        for(Map<String,String> row : context){
            String name = row.get(ValidationUtils.NAME_NAME);
            if(name == null){
                throw new RuntimeException("Column definition " + row + " must have field " + ValidationUtils.NAME_NAME + " specified");
            }
            String columnType = row.get(ValidationUtils.COLUMN_TYPE_NAME);
            if(columnType == null){
                throw new RuntimeException("Column definition " + row + " must have field " + ValidationUtils.NAME_NAME + " specified");
            }
            result.add(ColumnHolderUtils.createColumnHolder(name, ColumnType.valueOf(columnType)));
        }
        return result;
    }

    @And("^reset data on operator \"([^\"]*)\"$")
    public void reset_data_on_operator(String operatorName) throws Throwable {
        testMixerContext.getOperator(operatorName).resetData();
    }
}
