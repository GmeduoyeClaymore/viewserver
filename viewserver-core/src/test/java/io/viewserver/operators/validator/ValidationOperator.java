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

package io.viewserver.operators.validator;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.InputBase;
import io.viewserver.operators.OperatorBase;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;
import cucumber.api.DataTable;

import java.util.*;

/**
 * Created by bemm on 01/12/2014.
 */
public class ValidationOperator extends OperatorBase{

    private final Input input;
    private List validationActions = new ArrayList<>();
    private List expectedActions;
    private boolean validateOnCommit = true;

    public ValidationOperator(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);
        input = new Input(Constants.IN, this);
        addInput(input);
        register();
    }

    public void setExpected(List expectedActions) {
        this.expectedActions = expectedActions;
    }


    public void setValidateOnCommit(boolean validateOnCommit) {
        this.validateOnCommit = validateOnCommit;
    }

    @Override
    protected void commit() {
        super.commit();
        if(validateOnCommit) {
            validate();
        }
    }

    public void validate() {
        if(this.expectedActions != null){
            System.out.println(this.getName() + " validating");
            String[] expectedKeys = getKeysForActions(expectedActions);
            if (expectedKeys.length > 0) {
                DataTable dataTable = convertToTable(this.expectedActions, expectedKeys);
                DataTable other = convertToTable(this.validationActions, expectedKeys);
                dataTable.diff(other);
            }
        }
    }

    public void clearRecordedEvents(){
        validationActions.clear();
    }

    private String[] getKeysForActions(List expectedActions1) {
        List<HashMap<String,Object>> expectedRows = getRows(expectedActions1);
        return getKeys(expectedRows);
    }

    private DataTable convertToTable(List expectedActions,String[] keys) {
        List<HashMap<String,Object>> rows = getRows(expectedActions);
        List<List<String>>tableData = new ArrayList<>();
        List<String> keysList = ValidationUtils.getKeys(keys);
        tableData.add(keysList);
        for(HashMap<String,Object> row : rows){
            List<String> result = new ArrayList<String>();
            for(String key : keysList){
                Object val = row.get(key);
                if(val == null){
                    result.add("");
                }
                else{
                    result.add(val + "");
                }

            }
            tableData.add(result);
        }
        return DataTable.create(tableData);
    }

    private List<HashMap<String, Object>> getRows(List expectedActions) {
        List<HashMap<String,Object>> result = new ArrayList<>();
        for(Object expected : expectedActions){
            result.add(ValidationUtils.from(expected));
        }
        String[] keys = getKeys(result);
        for(Map<String,Object> entry : result){
            for(String key : keys){
                if(ValidationUtils.IGNORED_COLUMNS.contains(key))
                    continue;
                if(!entry.containsKey(key) || entry.get(key) == null || "null".equals(entry.get(key))){
                    entry.put(key,"");
                }
            }
        }
        result.sort(new Comparator<HashMap<String, Object>>() {
            @Override
            public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                Object id1 = o1.get("id");
                Object id2 = o2.get("id");
                if(id1 == null && id2 != null){
                    return -1;
                }
                if(id2 == null && id1 != null){
                    return 1;
                }

                if(id1 == null && id2 == null){
                    return 0;
                }
                return id1.toString().compareTo(id2.toString());
            }
        });
        return result;
    }

    private String[] getKeys(List<HashMap<String, Object>> result) {
        List<String> keys = new ArrayList<>();
        for(Map<String,Object> entry : result){
            for(String key : entry.keySet()){
                if(!keys.contains(key)){
                    keys.add(key);
                }
            }
        }
        return keys.toArray(new String[keys.size()]);
    }

    private class Input extends InputBase {

        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            validationActions.add(createValidationColumn(columnHolder, ValidationAction.Add));
            super.onColumnAdd(columnHolder);
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            validationActions.add(createValidationColumn(columnHolder, ValidationAction.Remove));
            super.onColumnRemove(columnHolder);
        }

        @Override
        protected void onRowAdd(int row) {
            validationActions.add(getValidationOperatorRow(row, ValidationAction.Add));
            super.onRowAdd(row);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            validationActions.add(getValidationOperatorRow(row, ValidationAction.Update));
            super.onRowUpdate(row, rowFlags);
        }

        @Override
        protected void onRowRemove(int row) {
            validationActions.add(getValidationOperatorRow(row, ValidationAction.Remove));
            super.onRowRemove(row);
        }

        @Override
        protected void onDataReset() {
            validationActions.add(new ValidationDataReset());
            super.onDataReset();
        }
        @Override
        protected void onSchemaReset() {
            validationActions.add(new ValidationSchemaReset());
            super.onSchemaReset();
        }

        private ValidationOperatorColumn createValidationColumn(ColumnHolder columnHolder, ValidationAction remove) {
            return new ValidationOperatorColumn(columnHolder.getName(), columnHolder.getType(), columnHolder.getColumnId(), remove);
        }

        private ValidationOperatorRow getValidationOperatorRow(int row, ValidationAction action) {
            HashMap<String,Object> values = new HashMap<>();
            HashMap<String,Object> previous = new HashMap<String,Object>();
            List<ColumnHolder> columnHolders = input.getProducer().getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder holder = columnHolders.get(i);
                if(holder != null){
                    Object value = !ValidationAction.Remove.equals(action) ? ColumnHolderUtils.getValue(holder, row) : null;
                    values.put(holder.getName(), value);
                    if(holder.supportsPreviousValues()) {
                        previous.put(holder.getName(),!action.equals(ValidationAction.Remove) ? getPreviousValue(row, holder) : null);
                    }
                }
            }
            return new ValidationOperatorRow(row, values, previous, action);
        }



    }

    private Object getPreviousValue(int row, ColumnHolder holder) {
        Object previousValue = ColumnHolderUtils.getPreviousValue(holder, row);
        if(previousValue == holder.getType().getDefaultValue()){
            return null;
        }
        return previousValue;
    }
}
