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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nick on 17/09/15.
 */
public class Record implements IRecord {
    protected final Map<String, Object> values = new HashMap<>();

    public static Record from(IRecord source) {
        Record record = new Record();
        record.initialiseFromRecord(source);
        return record;
    }

    public Record() {}

    void initialiseFromRecord(IRecord source) {
        values.clear();
        String[] columnNames = source.getColumnNames();
        int count = columnNames.length;
        for (int i = 0; i < count; i++) {
            String columnName = columnNames[i];
            values.put(columnName, source.getValue(columnName));
        }
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
        return (String)values.get(columnName);
    }

    @Override
    public boolean getBool(String columnName) {
        Object value = values.get(columnName);
        if (value instanceof NullableBool) {
            return ((NullableBool) value).getBooleanValue();
        }
        return (boolean) value;
    }

    @Override
    public NullableBool getNullableBool(String columnName) {
        return (NullableBool) values.get(columnName);
    }

    @Override
    public short getShort(String columnName) {
        return (short)values.get(columnName);
    }

    @Override
    public int getInt(String columnName) {
        try {
            return (int) values.get(columnName);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long getLong(String columnName) {
        return (long)values.get(columnName);
    }

    @Override
    public float getFloat(String columnName) {
        return (float)values.get(columnName);
    }

    @Override
    public double getDouble(String columnName) {
        return (double)values.get(columnName);
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
}
