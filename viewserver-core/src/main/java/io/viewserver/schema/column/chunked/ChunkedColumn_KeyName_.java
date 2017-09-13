// :_KeyName_=Bool,_KeyType_=boolean;_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double;_KeyName_=String,_KeyType_=String

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

package io.viewserver.schema.column.chunked;

import io.viewserver.core._KeyType_;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.*;

/**
 * Created by nickc on 23/09/2014.
 */
public class ChunkedColumn_KeyName_ extends Column_KeyName_Base implements IWritableColumn_KeyName_, IGrowableColumn {
    private static final ColumnType columnType = ColumnType._KeyName_;
    public static final _KeyType_ DEFAULT_VALUE = (_KeyType_)columnType.getDefaultValue();
    private ChunkedArray_KeyName_ data;
    private ChunkedArray_KeyName_ previousValues;
    private ColumnHolder holder;
    private IColumnWatcher columnWatcher;
    private int chunkSize;
    private ITableStorage tableStorage;

    public ChunkedColumn_KeyName_(ColumnHolder holder, IColumnWatcher columnWatcher, ITableStorage tableStorage, int capacity, int chunkSize) {
        super(holder.getName());
        this.holder = holder;
        this.columnWatcher = columnWatcher;
        this.tableStorage = tableStorage;
        this.chunkSize = chunkSize;

        data = new ChunkedArray_KeyName_(holder.getName(), capacity, chunkSize, DEFAULT_VALUE);
    }

    @Override
    public void storePreviousValues() {
        if (previousValues == null) {
            previousValues = new ChunkedArray_KeyName_(this.getName() + "_previous", data.getCapacity(), chunkSize, DEFAULT_VALUE);
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
    public void set_KeyName_(int row, _KeyType_ value) {
        ensureCapacity(row + 1);
        _KeyType_ currentValue = data.getValue(row);
        if ((!columnType.isObject() && currentValue == value)
                || (columnType.isObject() && columnType.areEqual(currentValue, value))) {
            return;
        }

        if (supportsPreviousValues() && !columnWatcher.isDirty(row, holder.getColumnId())) {
            previousValues.setValue(row, currentValue);
        }

        data.setValue(row, value);
        columnWatcher.markDirty(row, holder.getColumnId());
    }

    @Override
    public _KeyType_ get_KeyName_(int row) {
        return data.getValue(row);
    }

    @Override
    public _KeyType_ getPrevious_KeyName_(int row) {
        if (!supportsPreviousValues()) {
            throw new IllegalStateException("Previous values are not enabled for this column");
        }
        if (!columnWatcher.isDirty(row, holder.getColumnId())) {
            return get_KeyName_(row);
        }
        return previousValues.getValue(row);
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
