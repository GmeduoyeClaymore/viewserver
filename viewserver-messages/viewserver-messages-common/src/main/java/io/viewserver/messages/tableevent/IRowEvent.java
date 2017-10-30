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

package io.viewserver.messages.tableevent;

import io.viewserver.messages.IRecyclableMessage;
import io.viewserver.messages.common.ColumnType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 02/12/15.
 */
public interface IRowEvent<T> extends IRecyclableMessage<T> {
    Type getType();

    IRowEvent setType(Type type);

    int getRowId();

    String getKey();

    IRowEvent setKey(String key);

    IRowEvent setRowId(int rowId);

    List<IColumnValue> getColumnValues();

    enum Type {
        Add,
        Update,
        Remove;
    }

    interface IColumnValue<T> extends IRecyclableMessage<T> {
        int getColumnId();
        IColumnValue setColumnId(int columnId);

        ColumnType getValueType();

        boolean getBooleanValue();
        IColumnValue setBooleanValue(boolean value);
        int getIntegerValue();
        IColumnValue setIntegerValue(int value);
        long getLongValue();
        IColumnValue setLongValue(long value);
        float getFloatValue();
        IColumnValue setFloatValue(float value);
        double getDoubleValue();
        IColumnValue setDoubleValue(double value);
        String getStringValue();
        IColumnValue setStringValue(String value);
        boolean getNullValue();
        IColumnValue setNullValue();
    }
}
