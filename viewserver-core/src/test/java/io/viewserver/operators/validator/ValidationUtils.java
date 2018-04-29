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

import io.viewserver.datasource.ContentType;

import java.util.*;

/**
 * Created by bemm on 01/12/2014.
 */
public class ValidationUtils {
    public static String ACTION_NAME = "~Action";
    public static String ID_NAME = "~TEId";
    public static String NAME_NAME = "~Name";
    public static String COLUMN_TYPE_NAME = "~ColumnType";
    public static String CURRENT_PREVIOUS_DELIMITER = " << ";

    public static  List<String> IGNORED_COLUMNS =  Arrays.asList(ID_NAME);

    public static void orderColumns(List<String> unorderedColumns){
        unorderedColumns.sort((o1, o2) -> {
            int indexOf1 = IGNORED_COLUMNS.indexOf(o1);
            int indexOf2 = IGNORED_COLUMNS.indexOf(o2);
            if(indexOf1 > -1 && indexOf2 > -1){
                return new Integer(indexOf1).compareTo(new Integer(indexOf2));
            }
            if(indexOf2 > -1){
                return 1;
            }
            if(indexOf1 > -1){
                return -1;
            }
            return o1.compareTo(o2);
        });
    }

    public static HashMap<String,Object> getRowValues(ValidationOperatorRow obj, HashMap<String, ValidationOperatorColumn> columnsByName, ValidationOperator.ITransform<Integer> getValueKey){
        HashMap<String,Object> result = new HashMap<>();
        String actionName = getObjectName(obj);
        result.put(ACTION_NAME,actionName);
        result.put(ID_NAME,getValueKey.call(obj));
        populateRowValues(obj,result, columnsByName);
        return result;
    }

    public static HashMap<String,Object> getColumnValues(ValidationOperatorColumn obj){
        HashMap<String,Object> result = new HashMap<>();
        String actionName = getObjectName(obj);
        result.put(ACTION_NAME,actionName);
        result.put(ID_NAME,obj.getColumnId());
        result.put(NAME_NAME,getName(obj));
        result.put(COLUMN_TYPE_NAME,getType(obj));
        return result;
    }

    public static  ValidationOperatorRow toRow(Map<String,String> row, String keyColumnName){
        String actionName = row.get(ACTION_NAME);
        if(actionName == null || !actionName.startsWith("Row")){
            throw new RuntimeException("Row must have a field named " + ACTION_NAME + " that starts with Row.. current row has action " + actionName + " row is " + row);
        }

        ValidationAction valAction = ValidationAction.valueOf(actionName.substring(3));
        String idString = row.get(keyColumnName);
        if("".equals(idString) || idString == null){
            throw new RuntimeException("Row " + row + " does not contain a field named " + keyColumnName);
        }

        HashMap<String,Object>  values = getValues(row,0);
        HashMap<String,Object>  previous = getValues(row,1);
        if(ValidationAction.Remove.equals(valAction)){
            for(Map.Entry entry : values.entrySet()){
                if(entry.getKey().equals(keyColumnName)){
                    continue;
                }
                entry.setValue(null);
            }
            for(Map.Entry entry : previous.entrySet()){
                if(entry.getKey().equals(keyColumnName)){
                    continue;
                }
                entry.setValue(null);
            }
        }
        return new ValidationOperatorRow(values,new HashMap<>(),valAction);
    }

    public static  ValidationOperatorColumn toColumn(Map<String,String> column){
        String actionName = column.get(ACTION_NAME);
        if(!actionName.startsWith("Column")){
            throw new RuntimeException("Column must have a field named " + ACTION_NAME + " that starts with Column.. current row has action " + actionName + " column is " + column);
        }

        ValidationAction valAction = ValidationAction.valueOf(actionName.substring(6));

        String columnName = column.get(NAME_NAME);
        String s = column.get(COLUMN_TYPE_NAME);
        if(s == null || "".equals(s)){
            throw new RuntimeException("No column type specified in event" + column);
        }
        ContentType columnType = ContentType.valueOf(s + "");
        return new ValidationOperatorColumn(columnName,columnType,valAction);
    }

    private static HashMap<String, Object> getValues(Map<String, String> row, int partIndex) {
        HashMap<String, Object> result = new HashMap<>();
        for(String key : row.keySet()){
            if(IGNORED_COLUMNS.contains(key)){
                continue;
            }
            Object val = row.get(key);
            if(val == null){
                continue;
            }

            String[] parts = val.toString().split(CURRENT_PREVIOUS_DELIMITER);
            if(partIndex < parts.length){
                result.put(key,parts[partIndex]);
            }
        }
        return result;
    }

    private static String getObjectName(Object obj) {
        if(obj instanceof ValidationOperatorColumn){
            return "Column" + ((ValidationOperatorColumn)obj).getValidationAction();
        }
        if(obj instanceof ValidationOperatorRow){
            return "Row" + ((ValidationOperatorRow)obj).getValidationAction();
        }
        if(obj instanceof ValidationControlEvent){
            return "SchemaReset";
        }
        if(obj instanceof ValidationControlEvent){
            return ((ValidationControlEvent)obj).getEventName();
        }
        throw new RuntimeException("Invalid validation object " + obj);
    }

    private static String getName(Object obj) {
        if(obj instanceof ValidationOperatorColumn){
            return ((ValidationOperatorColumn)obj).getName();
        }
        return null;
    }
    private static String getType(Object obj) {
        if(obj instanceof ValidationOperatorColumn){
            return ((ValidationOperatorColumn)obj).getType() + "";
        }
        return null;
    }
    private static void populateRowValues(ValidationOperatorRow obj, HashMap<String, Object> row, HashMap<String, ValidationOperatorColumn> columnsByName) {
        HashMap<String, Object> values = obj.getValues();
        HashMap<String, Object> previous = obj.getPreviousValue();

        List<String> keys = new ArrayList<>();
        for(String key: values.keySet()){
            keys.add(key);
        }
        for(String key: previous.keySet()){
            keys.add(key);
        }
        for(String key : keys){
            if(IGNORED_COLUMNS.contains(key)){
                continue;
            }
            ValidationOperatorColumn column = columnsByName.get(key);
            Object val = values.get(key);
            Object currentStr = null;
            if(val == null || "".equals(val)){
                currentStr = "";
            }else{
                currentStr = column == null ? val + "" :  column.getType().convertToContentType(val);
            }

            if(!row.containsKey(key)) {
                row.put(key, currentStr);
            }

        }
    }


    public static List<String> columns = new ArrayList<>();

    public static int columnHash(String name) {
        if(!columns.contains(name)){
            columns.add(name);
        }
        return columns.indexOf(name) - 50;
    }


    public static int rowKeyHash(String name) {
        if(!columns.contains(name)){
            columns.add(name);
        }
        return columns.indexOf(name) + 100;
    }
}
