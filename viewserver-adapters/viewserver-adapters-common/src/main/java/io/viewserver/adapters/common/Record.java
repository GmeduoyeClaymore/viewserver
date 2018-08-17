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

package io.viewserver.adapters.common;

import io.viewserver.core.NullableBool;
import io.viewserver.datasource.IRecord;
import org.apache.commons.beanutils.ConvertUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by bemm on 17/09/15.
 */
public class Record implements IRecord {
    protected final Map<String, Object> values = new HashMap<>();
    private int version = 0;



    public static Record from(IRecord source) {
        Record record = new Record();
        record.initialiseFromRecord(source);
        return record;
    }

    public Record() {}

    public void initialiseFromRecord(IRecord source) {
        values.clear();
        String[] columnNames = source.getColumnNames();
        int count = columnNames.length;
        for (int i = 0; i < count; i++) {
            String columnName = columnNames[i];
            values.put(columnName, source.getValue(columnName));
        }
    }

    public Record initialiseFromRecord(Map<String, Object> source) {
        values.clear();
        Set<String> columnNames = source.keySet();
        for(String col : columnNames){
            values.put(col, source.get(col));
        }
        return this;
    }

    public boolean hasValue(String columnName){
        return values.containsKey(columnName);
    }


    public Record addValue(String columnName, Object value){
        values.put(columnName, value);
        return this;
    }

    void clear() {
        values.clear();
    }

    @Override
    public String[] getColumnNames() {
        String[] columnNames = new String[values.size()];
        int i = 0;
        for (String columnName : values.keySet()) {
            columnNames[i++] = columnName;
        }
        return columnNames;
    }

    @Override
    public byte getByte(String columnName) {
        return (byte)values.get(columnName);
    }

    @Override
    public String getString(String columnName) {
        Object o = values.get(columnName);
        return o == null || o instanceof String ? (String)o : (String)ConvertUtils.convert(o,String.class);
    }

    @Override
    public Boolean getBool(String columnName) {
        Object value = values.get(columnName);
        if (value instanceof NullableBool) {
            return ((NullableBool) value).getBooleanValue();
        }
        if(value == null){
            return false;
        }
        return (boolean) value;
    }

    @Override
    public NullableBool getNullableBool(String columnName) {
        return (NullableBool) values.get(columnName);
    }

    @Override
    public Short getShort(String columnName) {
        try {
            Object o = values.get(columnName);
            if(o == null){
                return null;
            }
            return o instanceof Short ? (Short)o : (Short)ConvertUtils.convert(o, Short.class);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Integer  getInt(String columnName) {
        try {
            Object o = values.get(columnName);
            if(o == null){
                return null;
            }
            return o instanceof Integer ? (Integer)o : (Integer)ConvertUtils.convert(o, Integer.class);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Long getLong(String columnName) {
        try {
            Object o = values.get(columnName);
            if(o == null){
                return null;
            }
            return o instanceof Long ? (Long)o : (Long)ConvertUtils.convert(o, Long.class);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Float getFloat(String columnName) {

        try {
            Object o = values.get(columnName);
            if(o == null){
                return null;
            }
            return o instanceof Float ? (Float)o : (Float)ConvertUtils.convert(o, Float.class);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Double getDouble(String columnName) {
        try {
            Object o = values.get(columnName);
            if(o == null){
                return null;
            }
            return o instanceof Double ? (Double)o : (Double)ConvertUtils.convert(o, Double.class);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Date getDate(String columnName) {
        return (Date)values.get(columnName);
    }

    @Override
    public Date getDateTime(String columnName) {
        return (Date)values.get(columnName);
    }

    @Override
    public Object getValue(String columnName) {
        return values.get(columnName);
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
