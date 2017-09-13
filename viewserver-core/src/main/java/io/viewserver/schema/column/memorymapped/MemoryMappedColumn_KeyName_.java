// :_KeyName_=Byte,_KeyType_=byte;_KeyName_=Short,_KeyType_=short;_KeyName_=Int,_KeyType_=int;_KeyName_=Long,_KeyType_=long;_KeyName_=Float,_KeyType_=float;_KeyName_=Double,_KeyType_=double

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

package io.viewserver.schema.column.memorymapped;

import io.viewserver.core._KeyType_;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnType;
import io.viewserver.schema.column.IColumnWatcher;
import io.viewserver.schema.column.IWritableColumn_KeyName_;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by nick on 27/10/15.
 */
public class MemoryMappedColumn_KeyName_ extends MemoryMappedColumnBase implements IWritableColumn_KeyName_ {
    private static final ColumnType columnType = ColumnType._KeyName_;
    private static final int BYTES = columnType.getSize() / Byte.SIZE;
    private static final _KeyType_ DEFAULT_VALUE = (_KeyType_) columnType.getDefaultValue();
    private final int columnId;
    private final IColumnWatcher columnWatcher;

    public MemoryMappedColumn_KeyName_(ColumnHolder holder, IColumnWatcher columnWatcher, Path schemaDirectory, int maxRows) throws IOException {
        super(schemaDirectory.resolve(holder.getName()).toFile(), maxRows, BYTES);
        this.columnWatcher = columnWatcher;
        columnId = holder.getColumnId();
    }

    @Override
    public _KeyType_ get_KeyName_(int row) {
        return UnsafeWrapper.get_KeyName_(address, row);
    }

    @Override
    public _KeyType_ getPrevious_KeyName_(int row) {
        if (!supportsPreviousValues()) {
            throw new IllegalStateException("Previous values are not enabled for this column");
        }
        if (!columnWatcher.isDirty(row, columnId)) {
            return get_KeyName_(row);
        }
        return UnsafeWrapper.get_KeyName_(prevAddress, row);
    }

    @Override
    public void set_KeyName_(int row, _KeyType_ value) {
        _KeyType_ currentValue = get_KeyName_(row);
        if ((!columnType.isObject() && currentValue == value)
                || (columnType.isObject() && columnType.areEqual(currentValue, value))) {
            return;
        }

        if (supportsPreviousValues() && !columnWatcher.isDirty(row, columnId)) {
            UnsafeWrapper.put_KeyName_(prevAddress, row, currentValue);
        }

        UnsafeWrapper.put_KeyName_(address, row, value);
        columnWatcher.markDirty(row, columnId);
    }

    @Override
    public ColumnType getType() {
        return ColumnType._KeyName_;
    }
}
