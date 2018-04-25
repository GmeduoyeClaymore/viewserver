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

/**
 * Created by bemm on 01/12/2014.
 */
public class ValidationOperator extends OperatorBase{

    private final Input input;
    private List<ValidationOperatorRow> validationRows = new ArrayList<>();
    private HashMap<Integer,ValidationOperatorRow> rowMapSoWeKnowValuesOnARemovedRow = new HashMap<>();
    private List<ValidationOperatorColumn> validationColumns = new ArrayList<>();
    private List<ValidationControlEvent> validationControlEvents = new ArrayList<>();

    private boolean validateOnCommit = true;
    private HashMap<String,ValidationOperatorColumn> columnsByName;



    public interface ITransform<T>{
        T call(Object param);
    }

    public ValidationOperator(String name,  IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);
        input = new Input(Constants.IN, this);
        columnsByName = new HashMap<>();
        addInput(input);
        register();
    }



    public void setValidateOnCommit(boolean validateOnCommit) {
        this.validateOnCommit = validateOnCommit;
    }

    @Override
    protected void commit() {
        super.commit();
    }


    public void validateColumns(List<ValidationOperatorColumn> expectedColumns) {
        System.out.println(this.getName() + " validating columns");
        DataTable dataTable = convertColumnsToTable(expectedColumns);
        DataTable other = convertColumnsToTable(this.validationColumns);
        try {
            dataTable.diff(other);
        }catch (Throwable throwable){

            throw throwable;
        }finally {
            System.out.println("Actual actions are \n" + dataTable);
        }
    }

    public void validateRows(ITransform<String> transform, List<ValidationOperatorRow> expectedRows, List<String> columns, String keyColumnName) {
        System.out.println(this.getName() + " validating rows");
        DataTable dataTable = convertRowsToTable(transform,expectedRows,columns, null, keyColumnName);
        DataTable other = convertRowsToTable(transform,this.validationRows,columns, expectedRows, keyColumnName);
        try {
            other.diff(dataTable);
        }catch (Throwable throwable){

            throw throwable;
        }finally {
            System.out.println("Actual actions are \n" + dataTable);
        }
    }


    public void clearRecordedEvents(){
        columnsByName.clear();
        validationRows.clear();
        validationColumns.clear();
    }


    private DataTable convertColumnsToTable(List<ValidationOperatorColumn> actionsToConvert) {

        List<HashMap<String,Object>> columns = new ArrayList<>();
        for(ValidationOperatorColumn column : actionsToConvert){
            columns.add(ValidationUtils.getColumnValues(column));
        }

        columns.sort((o1, o2) -> {
            Integer id1 = (Integer) o1.get(ValidationUtils.ID_NAME);
            Integer id2 = (Integer) o2.get(ValidationUtils.ID_NAME);
            if(id1 == null && id2 != null){
                return -1;
            }
            if(id2 == null && id1 != null){
                return 1;
            }

            if(id1 == null && id2 == null){
                return 0;
            }
            return id1.compareTo(id2);
        });
        return DataTable.create(columns);
    }

    private DataTable convertRowsToTable(ITransform<String> transform,List<ValidationOperatorRow> actionsToConvert,List<String> columns, List referenceActions, String keyColumn) {

        for(ValidationOperatorRow row : actionsToConvert){
            if(ValidationAction.Remove.equals(row.getValidationAction())){
                for(Map.Entry entry : row.getValues().entrySet()){
                    if(entry.getKey().equals(keyColumn)){
                        continue;
                    }
                    entry.setValue(null);
                }
                for(Map.Entry entry : row.getValues().entrySet()){
                    if(entry.getKey().equals(keyColumn)){
                        continue;
                    }
                    entry.setValue(null);
                }
            }
        }

        List<HashMap<String,Object>> rows = getRows(actionsToConvert, c-> ((ValidationOperatorRow)c).getRowId(keyColumn));
        if(columns == null){
            columns = rows.size() > 0 ? new ArrayList<>(rows.get(0).keySet()) : new ArrayList<>();
        }
        List<HashMap<String,Object>> referenceActionsRows = referenceActions == null ? null : getRows(referenceActions, c-> ((ValidationOperatorRow)c).getRowId(keyColumn));
        List<List<String>>tableData = new ArrayList<>();
        tableData.add(columns);
        for(HashMap<String,Object> row : rows){
            List<String> result = new ArrayList<String>();
            for(String key : columns){
                Object val = row.get(key);
                if(val == null || "".equals(val)){
                    result.add("");
                }
                else{
                    ValidationOperatorColumn out = columnsByName.get(key);
                    if(referenceActions != null && out!= null && out.getType().equals(ContentType.Json)){//basically if it is JSON then look at the reference row and only compare properties found in the source row
                        HashMap<String,Object> me =  val instanceof HashMap ? (HashMap)val : ControllerUtils.mapDefault(val + "");
                        Integer id = (Integer) row.get(ValidationUtils.ID_NAME);
                        HashMap<String, Object> referenceAction = referenceActionsRows.stream().filter(c -> isRowWithId(id,c)).findFirst().get();
                        if(referenceAction == null){
                            result.add(transform.call(val + ""));
                            continue;
                        }
                        HashMap<String,Object> reference = ControllerUtils.mapDefault((String) referenceAction.get(key));
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
        }
        return DataTable.create(tableData);
    }

    private boolean isRowWithId(Integer id, HashMap<String, Object> c) {
        return c.get(ValidationUtils.ID_NAME).equals(id);
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

    private List<HashMap<String, Object>> getRows(List<ValidationOperatorRow> expectedActions,ITransform<Integer> getValueKey) {
        List<HashMap<String,Object>> result = new ArrayList<>();
        for(ValidationOperatorRow expected : expectedActions){
            result.add(ValidationUtils.getRowValues(expected, this.columnsByName, getValueKey));
        }
        String[] keys = getKeys(expectedActions);
        for(Map<String,Object> entry : result){
            for(String key : keys){
                if(ValidationUtils.IGNORED_COLUMNS.contains(key))
                    continue;
                if(!entry.containsKey(key) || entry.get(key) == null || "null".equals(entry.get(key))){
                    entry.put(key,"");
                }
            }
        }
        result.sort((o1, o2) -> {
            Integer id1 = (Integer) o1.get(ValidationUtils.ID_NAME);
            Integer id2 = (Integer) o2.get(ValidationUtils.ID_NAME);
            if(id1 == null && id2 != null){
                return -1;
            }
            if(id2 == null && id1 != null){
                return 1;
            }

            if(id1 == null && id2 == null){
                return 0;
            }
            return id1.compareTo(id2);
        });
        return result;
    }

    private String[] getKeys(List<ValidationOperatorRow> result) {
        List<String> keys = new ArrayList<>();
        for(ValidationOperatorRow entry : result){
            for(String key : entry.getValues().keySet()){
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
            validationColumns.add(createValidationColumn(columnHolder, ValidationAction.Add));
            super.onColumnAdd(columnHolder);
        }

        @Override
        protected void onColumnRemove(ColumnHolder columnHolder) {
            validationColumns.add(createValidationColumn(columnHolder, ValidationAction.Remove));
            super.onColumnRemove(columnHolder);
        }

        @Override
        protected void onRowAdd(int row) {
            ValidationOperatorRow validationOperatorRow = getValidationOperatorRow(row, ValidationAction.Add);
            rowMapSoWeKnowValuesOnARemovedRow.put(row,validationOperatorRow);
            validationRows.add(validationOperatorRow);
            super.onRowAdd(row);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            ValidationOperatorRow validationOperatorRow = getValidationOperatorRow(row, ValidationAction.Update);
            rowMapSoWeKnowValuesOnARemovedRow.put(row,validationOperatorRow);
            validationRows.add(validationOperatorRow);
            super.onRowUpdate(row, rowFlags);
        }

        @Override
        protected void onRowRemove(int row) {
            ValidationOperatorRow addedRow = rowMapSoWeKnowValuesOnARemovedRow.get(row);
            validationRows.add(addedRow.clone(ValidationAction.Remove));
            super.onRowRemove(row);
        }

        @Override
        protected void onDataReset() {
            validationControlEvents.add(new ValidationControlEvent("DataReset"));
            super.onDataReset();
        }
        @Override
        protected void onSchemaReset() {
            validationControlEvents.add(new ValidationControlEvent("SchemaReset"));
            super.onSchemaReset();
        }

        private ValidationOperatorColumn createValidationColumn(ColumnHolder columnHolder, ValidationAction remove) {
            ValidationOperatorColumn validationOperatorColumn = new ValidationOperatorColumn(columnHolder.getName(), columnHolder.getMetadata().getDataType(), remove);
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

            return new ValidationOperatorRow(values, previous, action);
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
