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
import java.util.stream.Collectors;

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
        unorderedColumns.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
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

            }
        });

    }

    public static HashMap<String,Object> from(Object obj, HashMap<String, ValidationOperatorColumn> columnsByName){
        HashMap<String,Object> result = new HashMap<>();
        String actionName = getObjectName(obj);
        result.put(ACTION_NAME,actionName);
        result.put(ID_NAME,getId(obj));
        result.put(NAME_NAME,getName(obj));
        result.put(COLUMN_TYPE_NAME,getType(obj));
        populateValues(obj,result, columnsByName);
        return result;
    }
    public static  Object to(Map<String,String> row){
        String actionName = (String) row.get(ACTION_NAME);
        if("".equals(actionName) || actionName == null){
            throw new RuntimeException("Row must specify an action name \"" + actionName + "\"");
        }
        if(actionName.equals("SchemaReset")){
            return new ValidationSchemaReset();
        }
        if(actionName.equals("DataReset")){
            return new ValidationDataReset();
        }
        if(actionName.startsWith("Row")){
            ValidationAction valAction = ValidationAction.valueOf(actionName.substring(3));
            String idString = row.get(ID_NAME);
            if("".equals(idString) || idString == null){
                throw new RuntimeException("Row " + row + " does not contain a field named " + ID_NAME);
            }
            Integer id = Integer.parseInt(idString + "");

            HashMap<String,Object>  values = getValues(row,0);
            HashMap<String,Object>  previous = getValues(row,1);
            if(ValidationAction.Remove.equals(valAction)){
               for(Map.Entry entry : values.entrySet()){
                   entry.setValue(null);
               }
               for(Map.Entry entry : previous.entrySet()){
                   entry.setValue(null);
               }
            }
            return new ValidationOperatorRow(id,values,new HashMap<>(),valAction);
        }
        if(actionName.startsWith("Column")){
            ValidationAction valAction = ValidationAction.valueOf(actionName.substring(6));
            String columnId = row.get(ID_NAME);
            if(columnId == null){
                throw new RuntimeException("Column definition must have a field " + ID_NAME + " (" + row + ")");
            }
            Integer id = Integer.parseInt(columnId + "");
            String columnName = (String) row.get(NAME_NAME);
            String s = row.get(COLUMN_TYPE_NAME);
            if(s == null || "".equals(s)){
                throw new RuntimeException("No column type specified in event" + row);
            }
            ContentType columnType = ContentType.valueOf(s + "");
            return new ValidationOperatorColumn(columnName,columnType,id,valAction);
        }
        throw new RuntimeException("Unrecognised validation action " + actionName);
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
        if(obj instanceof ValidationSchemaReset){
            return "SchemaReset";
        }
        if(obj instanceof ValidationDataReset){
            return "DataReset";
        }
        throw new RuntimeException("Invalid validation object " + obj);
    }
    private static Integer getId(Object obj) {
        if(obj instanceof ValidationOperatorColumn){
            return ((ValidationOperatorColumn)obj).getColumnId();
        }
        if(obj instanceof ValidationOperatorRow){
            return ((ValidationOperatorRow)obj).getRowId();
        }
        return null;
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
    private static void populateValues(Object obj, HashMap<String, Object> row, HashMap<String, ValidationOperatorColumn> columnsByName) {

        if(obj instanceof ValidationOperatorRow){
            HashMap<String, Object> values = ((ValidationOperatorRow) obj).getValues();
            HashMap<String, Object> previous = ((ValidationOperatorRow) obj).getPreviousValue();

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
                if(val == null){
                    currentStr = "";
                }else{
                    currentStr = column == null ? val + "" :  column.getType().convertToContentType(val);
                }

                if(!row.containsKey(key)) {
                    row.put(key, currentStr);
                }

            }
        }
    }

    public static List<String> getKeys(String[] keys) {
        List<String> keysList = Arrays.asList(keys);
        keysList = keysList.stream().filter(key -> !IGNORED_COLUMNS.contains(key)).collect(Collectors.toList());
        ValidationUtils.orderColumns(keysList);
        return keysList;
    }
}
