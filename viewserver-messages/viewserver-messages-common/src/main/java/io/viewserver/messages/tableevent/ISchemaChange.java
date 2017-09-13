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

import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.IRecyclableMessage;
import io.viewserver.messages.common.ColumnType;

import java.util.List;

/**
 * Created by nick on 02/12/15.
 */
public interface ISchemaChange<T> extends IPoolableMessage<T> {
    int getSchemaSize();

    ISchemaChange setSchemaSize(int schemaSize);

    List<IAddColumn> getAddColumns();

    List<IRemoveColumn> getRemoveColumns();

    interface IAddColumn<T> extends IRecyclableMessage<T> {
        int getColumnId();

        IAddColumn setColumnId(int columnId);

        ColumnType getType();

        IAddColumn setType(ColumnType type);

        String getName();

        IAddColumn setName(String name);

        DataType getDataType();

        IAddColumn setDataType(DataType dataType);

        boolean getBooleanNullValue();
        IAddColumn setBooleanNullValue(boolean nullValue);
        int getIntegerNullValue();
        IAddColumn setIntegerNullValue(int nullValue);
        long getLongNullValue();
        IAddColumn setLongNullValue(long nullValue);
        float getFloatNullValue();
        IAddColumn setFloatNullValue(float nullValue);
        double getDoubleNullValue();
        IAddColumn setDoubleNullValue(double nullValue);
        String getStringNullValue();
        IAddColumn setStringNullValue(String nullValue);
        boolean getNullNullValue();
        IAddColumn setNullNullValue();
    }

    interface IRemoveColumn<T> extends IRecyclableMessage<T> {
        int getColumnId();

        IRemoveColumn setColumnId(int columnId);
    }

    enum DataType {
        Boolean,
        NullableBoolean,
        Byte,
        Short,
        Integer,
        Long,
        Float,
        Double,
        String,
        Date,
        DateTime
    }
}
