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
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerUtils;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.ContentType;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.InputBase;
import io.viewserver.operators.OperatorBase;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;
import cucumber.api.DataTable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by bemm on 01/12/2014.
 */
public class ValidationOperator extends OperatorBase{

    private final Input input;
    private List validationActions = new ArrayList<>();
    private List expectedActions;
    private boolean validateOnCommit = true;
    private HashMap<String,ValidationOperatorColumn> columnsByName;

    public interface ITransform{
        String call(String param);
    }

    public ValidationOperator(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);
        input = new Input(Constants.IN, this);
        columnsByName = new HashMap<>();
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
        validate(c->c);
    }


    public void validate(ITransform transform) {
        if(this.expectedActions != null){
            System.out.println(this.getName() + " validating");
            String[] expectedKeys = getKeysForActions(expectedActions);
            if (expectedKeys.length > 0) {
                DataTable dataTable = convertToTable(transform,this.expectedActions, expectedKeys, null);
                DataTable other = convertToTable(transform, this.validationActions, expectedKeys, this.expectedActions);
                try {
                    dataTable.diff(other);
                }catch (Throwable throwable){
                    DataTable actualActions = convertToTable(transform, this.validationActions, null, null);
                    System.out.println("Actual actions are \n" + actualActions);
                    throw throwable;
                }
            }
        }
    }


    public void clearRecordedEvents(){
        columnsByName.clear();
        validationActions.clear();
    }

    private String[] getKeysForActions(List expectedActions1) {
        List<HashMap<String,Object>> expectedRows = getRows(expectedActions1);
        return getKeys(expectedRows);
    }



    private DataTable convertToTable(ITransform transform,List actionsToConvert,String[] keys, List referenceActions) {
        List<HashMap<String,Object>> rows = getRows(actionsToConvert);
        List<List<String>>tableData = new ArrayList<>();
        List<String> keysList = null;
        if(keys != null){
            keysList = ValidationUtils.getKeys(keys);
        }else{
            if(rows.size() > 0){
                keysList = new ArrayList<>(rows.get(0).keySet());
            }else{
                keysList = new ArrayList<>();
            }
        };
        tableData.add(keysList);
        int rowCounter = 0;
        for(HashMap<String,Object> row : rows){
            List<String> result = new ArrayList<String>();
            for(String key : keysList){

                Object val = row.get(key);
                if(val == null || "".equals(val)){
                    result.add("");
                }
                else{
                    ValidationOperatorColumn out = columnsByName.get(key);
                    if(referenceActions != null && out!= null && out.getType().equals(ContentType.Json)){//basically if it is JSON then look at the reference row and only compare properties found in the source row
                        HashMap<String,Object> me =  (HashMap<String, Object>) val;
                        ValidationOperatorRow referenceAction = (ValidationOperatorRow) referenceActions.get(rowCounter);
                        if(referenceAction == null){
                            result.add(transform.call(val + ""));
                            continue;
                        }
                        HashMap<String,Object> reference = ControllerUtils.mapDefault((String) referenceAction.getValues().get(key));
                        if(reference == null){
                            result.add(transform.call(val + ""));
                            continue;
                        }
                        HashMap<String, Object> propsToCompare = filterForReferenceProps(me, reference);
                        result.add(transform.call(ControllerUtils.toConsistentString(propsToCompare)));
                    }else{
                        result.add(transform.call(out == null ? val + "" : ControllerUtils.toConsistentString(out.getType().convertToContentType(val))));
                    }
                }

            }
            tableData.add(result);
            rowCounter++;
        }
        return DataTable.create(tableData);
    }

    private HashMap<String, Object> filterForReferenceProps(HashMap<String, Object> me, HashMap<String, Object> reference) {
        HashMap<String,Object> propsToCompare = new HashMap<>();
        for(Map.Entry<String,Object> referenceEntry : reference.entrySet()){
            Object value = me.get(referenceEntry.getKey());
            if(value == null){
                continue;
            }
            if(referenceEntry.getValue() instanceof HashMap && value instanceof HashMap) {
                propsToCompare.put(referenceEntry.getKey(), filterForReferenceProps((HashMap)value,(HashMap) referenceEntry.getValue()));
            }else{
                propsToCompare.put(referenceEntry.getKey(), value);
            }
        }
        return propsToCompare;
    }

    private List<HashMap<String, Object>> getRows(List expectedActions) {
        List<HashMap<String,Object>> result = new ArrayList<>();
        for(Object expected : expectedActions){
            result.add(ValidationUtils.from(expected, this.columnsByName));
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
            ValidationOperatorColumn validationOperatorColumn = new ValidationOperatorColumn(columnHolder.getName(), columnHolder.getMetadata().getDataType(), columnHolder.getColumnId(), remove);
            ValidationOperator.this.columnsByName.put(columnHolder.getName(), validationOperatorColumn);
            return validationOperatorColumn;
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
