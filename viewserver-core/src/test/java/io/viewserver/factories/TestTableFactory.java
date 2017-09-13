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

import io.viewserver.catalog.Catalog;
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.*;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import io.viewserver.steps.FieldDefinition;
import io.viewserver.steps.TestMixerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestTableFactory implements ITestOperatorFactory {

    private static final Logger logger = LoggerFactory.getLogger(TestMixerContext.class);

    private final Catalog catalog;
    private final ExecutionContext executionContext;

    public static String RECORDS_PARAM_NAME = "records";

    public TestTableFactory(ExecutionContext executionContext, Catalog catalog) {
        this.executionContext = executionContext;
        this.catalog = catalog;
    }


    @Override
    public String getOperatorType() {
        return "table";
    }
    @Override
    public IOperator create(String operatorName, Map<String, Object> context){
        ArrayList<Map<String,String>> records = ITestOperatorFactory.getParam(RECORDS_PARAM_NAME, context, new ArrayList<Map<String, String>>().getClass());
        List<FieldDefinition> definitions = getFieldDefinitions(records);

        Schema schema = new Schema();
        for(FieldDefinition definition : definitions){
            schema.addColumn(definition.getFieldName(), definition.getColumnType());
        }
        KeyedTable table = new KeyedTable(operatorName, executionContext, catalog, schema, new ChunkedColumnStorage(1024),new TableKeyDefinition("id"));
        table.initialise(records.size());
        for (Map<String, String> record : records) {
            table.addRow(new ITableRowUpdater() {
                @Override
                public void setValues(ITableRow row) {
                    for (FieldDefinition definition : definitions) {
                        definition.setValue(record, row);
                    }
                }
            });
        }

        return table;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config) {
        throw new RuntimeException("Unable to configure a table opeator");
    }

    private List<FieldDefinition> getFieldDefinitions(List<Map<String, String>> records) {
        List<String> fields = getFields(records);
        List<FieldDefinition> definitions = new ArrayList<FieldDefinition>();

        for (String field : fields) {
            try {
                FieldDefinition definition = new FieldDefinition(field);
                definitions.add(definition);
            } catch (Throwable ex) {
                logger.error(String.format("Problem processing field definition \"%s\"", field),ex);
            }
        }
        return definitions;
    }

    public void updateTable(String operatorName,List<Map<String,String>> records){
        KeyedTable table = (KeyedTable) catalog.getOperator(operatorName);
        if(table == null){
            throw new RuntimeException("Unable to find table named \"" + operatorName + "\"");
        }
        List<FieldDefinition> definitions = getFieldDefinitions(records);
        FieldDefinition idField = null;
        for(FieldDefinition def : definitions){
            if(def.getFieldName().equals("id")){
                idField = def;
                break;
            }
        }

        if(idField == null){
            throw new RuntimeException("Record set doesn't contain a field named id");
        }

        for(Map<String,String> record : records){
            Object id = idField.getValue(record);
            if(id == null){
                throw new RuntimeException("Row must contain an id");
            }
            TableKey key = new TableKey(id);
            if(table.getRow(key) == -1){
                table.addRow(key, row -> {
                    for(FieldDefinition def : definitions){
                        def.setValue(record,row);
                    }
                });
            } else{
                table.updateRow(key, row -> {
                    for(FieldDefinition def : definitions){
                        def.setValue(record,row);
                    }
                });
            }

        }
    }

    private List<String> getFields(List<Map<String, String>> records) {
        List<String> result = new ArrayList<>();
        for(Map<String,String> record : records){
            Set<String> keys = record.keySet();
            for(String key : keys){
                if(!result.contains(key)){
                    result.add(key);
                }
            }
        }
        return result;
    }

}
