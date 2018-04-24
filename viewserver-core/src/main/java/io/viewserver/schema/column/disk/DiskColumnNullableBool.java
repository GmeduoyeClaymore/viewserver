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
import io.viewserver.core.NullableBool;
import io.viewserver.schema.column.*;
import io.viewserver.schema.column.chunked.ChunkedArrayByte;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by bemm on 02/12/2014.
 */
public class DiskColumnNullableBool extends ColumnNullableBoolBase implements IDiskColumn, IWritableColumnNullableBool {
    private static final ColumnType columnType = ColumnType.NullableBool;
    private static final NullableBool[] values = { NullableBool.Null, NullableBool.False, NullableBool.True };
    private static final int BYTES = 1;
    private static final NullableBool DEFAULT_VALUE = NullableBool.Null;
    private static final int BUFFER_SIZE = 4096;
    private final Path path;
    private final FileChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBufferWrapper bufferWrapper = new ByteBufferWrapper(buffer);
    private int bufferStart = -1;
    private int bufferEnd = -1;
    private IntHashSet loadedRows;
    private ChunkedArrayByte data;
    private ChunkedArrayByte previousValues;
    private ColumnHolder holder;
    private IColumnWatcher columnWatcher;
    private boolean bufferChanged;

    public DiskColumnNullableBool(ColumnHolder holder, IColumnWatcher columnWatcher, Path schemaDirectory) {
        super(holder.getName());
        this.holder = holder;
        this.columnWatcher = columnWatcher;

        loadedRows = new IntHashSet(128, 0.75f, -1);
        data = new ChunkedArrayByte(holder.getName(), 1024, 1024, DEFAULT_VALUE.getNumericValue());

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
    public NullableBool getNullableBool(int row) {
        int index = loadedRows.addInt(row);
        if (index < 0) {
            // row was already loaded
            return values[data.getValue(-index - 1)];
        }
        prepareBuffer(row);
        byte value = bufferWrapper.getByte((row - bufferStart) * BYTES);
        ensureCapacity(index + 1);
        data.setValue(index, value);
        return values[value];
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
        int index = -1;
        if (!columnWatcher.isDirty(row, holder.getColumnId()) || (index = loadedRows.index(row)) == -1) {
            return getNullableBool(row);
        }
        return values[previousValues.getValue(index)];
    }

    @Override
    public void setNullableBool(int row, NullableBool value) {
        NullableBool currentValue = getNullableBool(row);
        if (currentValue == value) {
            return;
        }

        int index = -loadedRows.addInt(row) - 1; // the row has definitely been loaded, since we called getByte() above
        if (previousValues != null && !columnWatcher.isDirty(row, holder.getColumnId())) {
            previousValues.setValue(index, currentValue.getNumericValue());
        }

        byte newByteValue = value.getNumericValue();
        bufferWrapper.putByte((row - bufferStart) * BYTES, newByteValue);
        bufferChanged = true;

        ensureCapacity(index + 1);
        data.setValue(index, newByteValue);
        columnWatcher.markDirty(row, holder.getColumnId());
    }

    @Override
    public void storePreviousValues() {
        if (previousValues == null) {
            previousValues = new ChunkedArrayByte(this.getName() + "_previous", data.getCapacity(), 1024, DEFAULT_VALUE.getNumericValue());
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
