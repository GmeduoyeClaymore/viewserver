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

import io.viewserver.schema.column.ColumnFactoryBase;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IColumn;
import io.viewserver.schema.column.IColumnWatcher;

/**
 * Created by nickc on 26/09/2014.
 */
public class ChunkedColumnFactory extends ColumnFactoryBase {
    private int capacity;
    private final int chunkSize;

    public ChunkedColumnFactory(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public void initialise(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public IColumn createColumn(ColumnHolder columnHolder, IColumnWatcher columnWatcher) {
        switch (columnHolder.getType()) {
            case Bool: {
                return new ChunkedColumnBool(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case NullableBool: {
                return new ChunkedColumnNullableBool(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case Byte: {
                return new ChunkedColumnByte(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case Short: {
                return new ChunkedColumnShort(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case Int: {
                return new ChunkedColumnInt(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case Long: {
                return new ChunkedColumnLong(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case Float: {
                return new ChunkedColumnFloat(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case Double: {
                return new ChunkedColumnDouble(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            case String: {
                return new ChunkedColumnString(columnHolder, columnWatcher, getTableStorage(), capacity, chunkSize);
            }
            default: {
                throw new IllegalArgumentException("Cannot create chunked column for type '" + columnHolder.getType() + "'");
            }
        }
    }
}
