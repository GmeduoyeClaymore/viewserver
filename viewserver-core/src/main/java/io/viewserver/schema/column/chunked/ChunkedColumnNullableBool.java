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

// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double;_KeyName_=String,_KeyType_=String


package io.viewserver.schema.column.chunked;

import io.viewserver.core.NullableBool;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.*;

/**
 * Created by nickc on 23/09/2014.
 */
public class ChunkedColumnNullableBool extends ColumnNullableBoolBase implements IWritableColumnNullableBool, IGrowableColumn {
    private static final ColumnType columnType = ColumnType.NullableBool;
    private static final NullableBool[] values = { NullableBool.Null, NullableBool.False, NullableBool.True };
    public static final NullableBool DEFAULT_VALUE = NullableBool.Null;
    private ChunkedArrayByte data;
    private ChunkedArrayByte previousValues;
    private ColumnHolder holder;
    private IColumnWatcher columnWatcher;
    private int chunkSize;
    private ITableStorage tableStorage;

    public ChunkedColumnNullableBool(ColumnHolder holder, IColumnWatcher columnWatcher, ITableStorage tableStorage, int capacity, int chunkSize) {
        super(holder.getName());
        this.holder = holder;
        this.columnWatcher = columnWatcher;
        this.tableStorage = tableStorage;
        this.chunkSize = chunkSize;

        data = new ChunkedArrayByte(holder.getName(), capacity, chunkSize, DEFAULT_VALUE.getNumericValue());
    }

    @Override
    public void storePreviousValues() {
        if (previousValues == null) {
            previousValues = new ChunkedArrayByte(this.getName() + "_previous", data.getCapacity(), chunkSize, DEFAULT_VALUE.getNumericValue());
        }
    }

    @Override
    public void resetAll() {
        data.resetAll();
        if (supportsPreviousValues()) {
            previousValues.resetAll();
        }
    }

    @Override
    public void setNullableBool(int row, NullableBool value) {
        ensureCapacity(row + 1);
        byte currentValue = data.getValue(row);
        byte newByteValue = value.getNumericValue();
        if (currentValue == newByteValue) {
            return;
        }

        if (supportsPreviousValues() && !columnWatcher.isDirty(row, holder.getColumnId())) {
            previousValues.setValue(row, currentValue);
        }

        data.setValue(row, newByteValue);
        columnWatcher.markDirty(row, holder.getColumnId());
    }

    @Override
    public NullableBool getNullableBool(int row) {
        return values[data.getValue(row)];
    }

    @Override
    public boolean getBool(int row) {
        return getNullableBool(row) == NullableBool.True;
    }

    @Override
    public NullableBool getPreviousNullableBool(int row) {
        if (!supportsPreviousValues()) {
            throw new IllegalStateException("Previous values are not enabled for this column");
        }
        if (!columnWatcher.isDirty(row, holder.getColumnId())) {
            return getNullableBool(row);
        }
        return values[previousValues.getValue(row)];
    }

    @Override
    public boolean supportsPreviousValues() {
        return previousValues != null;
    }

    @Override
    public void ensureCapacity(int capacity) {
        data.ensureCapacity(capacity);
        if (previousValues != null) {
            previousValues.ensureCapacity(capacity);
        }
    }

    @Override
    public ITableStorage getTableStorage() {
        return tableStorage;
    }
}
