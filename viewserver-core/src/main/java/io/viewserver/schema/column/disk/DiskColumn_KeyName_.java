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

package io.viewserver.schema.column.disk;

import io.viewserver.collections.IntHashSet;
import io.viewserver.core._KeyType_;
import io.viewserver.schema.column.*;
import io.viewserver.schema.column.chunked.ChunkedArray_KeyName_;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by nickc on 02/12/2014.
 */
public class DiskColumn_KeyName_ extends Column_KeyName_Base implements IDiskColumn, IWritableColumn_KeyName_ {
    private static final ColumnType columnType = ColumnType._KeyName_;
    private static final int BYTES = columnType.getSize() / Byte.SIZE;
    private static final _KeyType_ DEFAULT_VALUE = (_KeyType_) columnType.getDefaultValue();
    private static final int BUFFER_SIZE = 4096;
    private final Path path;
    private final FileChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBufferWrapper bufferWrapper = new ByteBufferWrapper(buffer);
    private int bufferStart = -1;
    private int bufferEnd = -1;
    private IntHashSet loadedRows;
    private ChunkedArray_KeyName_ data;
    private ChunkedArray_KeyName_ previousValues;
    private ColumnHolder holder;
    private IColumnWatcher columnWatcher;
    private boolean bufferChanged;

    public DiskColumn_KeyName_(ColumnHolder holder, IColumnWatcher columnWatcher, Path schemaDirectory) {
        super(holder.getName());
        this.holder = holder;
        this.columnWatcher = columnWatcher;

        loadedRows = new IntHashSet(128, 0.75f, -1);
        data = new ChunkedArray_KeyName_(holder.getName(), 1024, 1024, DEFAULT_VALUE);

        path = new File(schemaDirectory.toFile(), holder.getName()).toPath();
        try {
            channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean supportsPreviousValues() {
        return previousValues != null;
    }

    @Override
    public _KeyType_ get_KeyName_(int row) {
        int index = loadedRows.addInt(row);
        if (index < 0) {
            // row was already loaded
            return data.getValue(-index - 1);
        }
        prepareBuffer(row);
        _KeyType_ value = bufferWrapper.get_KeyName_((row - bufferStart) * BYTES);
        ensureCapacity(index + 1);
        data.setValue(index, value);
        return value;
    }

    @Override
    public _KeyType_ getPrevious_KeyName_(int row) {
        if (!supportsPreviousValues()) {
            throw new IllegalStateException("Previous values are not enabled for this column");
        }
        int index = -1;
        if (!columnWatcher.isDirty(row, holder.getColumnId()) || (index = loadedRows.index(row)) == -1) {
            return get_KeyName_(row);
        }
        return previousValues.getValue(index);
    }

    @Override
    public void set_KeyName_(int row, _KeyType_ value) {
        _KeyType_ currentValue = get_KeyName_(row);
        if ((!columnType.isObject() && currentValue == value)
                || (columnType.isObject() && columnType.areEqual(currentValue, value))) {
            return;
        }

        int index = -loadedRows.addInt(row) - 1; // the row has definitely been loaded, since we called get_KeyName_() above
        if (previousValues != null && !columnWatcher.isDirty(row, holder.getColumnId())) {
            previousValues.setValue(index, currentValue);
        }

        bufferWrapper.put_KeyName_((row - bufferStart) * BYTES, value);
        bufferChanged = true;

        ensureCapacity(index + 1);
        data.setValue(index, value);
        columnWatcher.markDirty(row, holder.getColumnId());
    }

    @Override
    public void storePreviousValues() {
        if (previousValues == null) {
            previousValues = new ChunkedArray_KeyName_(this.getName() + "_previous", data.getCapacity(), 1024, DEFAULT_VALUE);
        }
    }

    @Override
    public void resetAll() {
        // TODO: reset file?
        data.resetAll();
        if (supportsPreviousValues()) {
            previousValues.resetAll();
        }
    }

    private void prepareBuffer(int row) {
        if (row >= bufferStart && row <= bufferEnd) {
            return;
        }

        try {
            if (bufferStart != -1) {
                buffer.rewind();
                if (bufferChanged) {
                    channel.write(buffer, bufferStart * BYTES);
                    bufferChanged = false;
                }
            }

            bufferStart = row - (row % (BUFFER_SIZE / BYTES));
            bufferEnd = bufferStart + buffer.capacity() / BYTES - 1;
            buffer.clear();
            channel.read(buffer, bufferStart * BYTES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureCapacity(int capacity) {
        data.ensureCapacity(capacity);
        if (supportsPreviousValues()) {
            previousValues.ensureCapacity(capacity);
        }
    }

    @Override
    public void unloadAll() {
        loadedRows.clear();
    }
}
