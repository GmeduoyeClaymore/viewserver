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

import io.viewserver.operators.table.ITableRow;
import io.viewserver.schema.column.ColumnType;

import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class FieldDefinition {
    private String fieldName;
    private ColumnType columnType;

    public static String DELIMITER = "~";

    public FieldDefinition(String fieldString) {
        int delimiterIndex = fieldString.indexOf(DELIMITER);
        if(delimiterIndex == -1){
            throw new RuntimeException("Field definition should be in format fieldName~columnType");
        }
        fieldName = fieldString.substring(0,delimiterIndex);
        columnType = ColumnType.valueOf(fieldString.substring(delimiterIndex+1));
    }

    public String getFieldName() {
        return fieldName;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setValue(Map<String, String> record, ITableRow row) {
        Object value = getValue(record);
        if(value == null){
            return;
        }
        if (columnType.equals(ColumnType.Bool)){
            row.setBool(fieldName, (Boolean) value);
        }
        else if (columnType.equals(ColumnType.Byte)){
            row.setByte(fieldName, (Byte) value);
        }
        else if (columnType.equals(ColumnType.Double)){
            row.setDouble(fieldName, (Double) value);
        }
        else if (columnType.equals(ColumnType.Float)){
            row.setFloat(fieldName, (Float) value);
        }
        else if (columnType.equals(ColumnType.Int)){
            row.setInt(fieldName, (Integer) value);
        }
        else if (columnType.equals(ColumnType.Long)){
            row.setLong(fieldName, (Long) value);
        }
        else if (columnType.equals(ColumnType.Short)){
            row.setShort(fieldName, (Short) value);
        }
        else if (columnType.equals(ColumnType.String)){
            row.setString(fieldName, (String) value);
        }

    }

    @Override
    public String toString() {
        return fieldName + DELIMITER + columnType;
    }

    public Object getValue(Map<String, String> record) {
        String value = record.get(toString());
        if(value == null){
            return null;
        }
        if (columnType.equals(ColumnType.Bool)){
                return Boolean.parseBoolean(value);
        }
        else if (columnType.equals(ColumnType.Byte)){
            return Byte.parseByte(value);
        }
        else if (columnType.equals(ColumnType.Double)){
            return Double.parseDouble(value);
        }
        else if (columnType.equals(ColumnType.Float)){
            return Float.parseFloat(value);
        }
        else if (columnType.equals(ColumnType.Int)){
            return Integer.parseInt(value);
        }
        else if (columnType.equals(ColumnType.Long)){
            return Long.parseLong(value);
        }
        else if (columnType.equals(ColumnType.Short)){
            return Short.parseShort(value);
        }
        else if (columnType.equals(ColumnType.String)){
            return value;
        }
        throw new RuntimeException(String.format("Cannot get value for field named \"%s\" as the column type \"%s\" is unknown",fieldName,columnType));
    }


}
