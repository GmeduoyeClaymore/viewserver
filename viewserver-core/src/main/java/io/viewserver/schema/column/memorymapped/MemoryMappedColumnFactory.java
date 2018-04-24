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

import io.viewserver.schema.column.ColumnFactoryBase;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IColumn;
import io.viewserver.schema.column.IColumnWatcher;

import java.io.IOException;

/**
 * Created by bemm on 26/09/2014.
 */
public class MemoryMappedColumnFactory extends ColumnFactoryBase {
    private int capacity = Integer.MAX_VALUE / 8;

    @Override
    public void initialise(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public IColumn createColumn(ColumnHolder columnHolder, IColumnWatcher columnWatcher) {
        try {
            switch (columnHolder.getType()) {
//            case Bool: {
//                return new MemoryMappedColumnBool(columnHolder, columnWatcher, ((MemoryMappedColumnStorage)getTableStorage()).getSchemaDirectory());
//            }
                case Byte: {
                    return new MemoryMappedColumnByte(columnHolder, columnWatcher, ((MemoryMappedColumnStorage) getTableStorage()).getSchemaDirectory(), capacity);
                }
                case Short: {
                    return new MemoryMappedColumnShort(columnHolder, columnWatcher, ((MemoryMappedColumnStorage) getTableStorage()).getSchemaDirectory(), capacity);
                }
                case Int: {
                    return new MemoryMappedColumnInt(columnHolder, columnWatcher, ((MemoryMappedColumnStorage) getTableStorage()).getSchemaDirectory(), capacity);
                }
                case Long: {
                    return new MemoryMappedColumnLong(columnHolder, columnWatcher, ((MemoryMappedColumnStorage) getTableStorage()).getSchemaDirectory(), capacity);
                }
                case Float: {
                    return new MemoryMappedColumnFloat(columnHolder, columnWatcher, ((MemoryMappedColumnStorage) getTableStorage()).getSchemaDirectory(), capacity);
                }
                case Double: {
                    return new MemoryMappedColumnDouble(columnHolder, columnWatcher, ((MemoryMappedColumnStorage) getTableStorage()).getSchemaDirectory(), capacity);
                }
//            case String: {
//                return new MemoryMappedColumnString(columnHolder, columnWatcher, ((MemoryMappedColumnStorage)getTableStorage()).getSchemaDirectory(), capacity);
//            }
                default: {
                    throw new IllegalArgumentException("Cannot create disk column for type '" + columnHolder.getType() + "'");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
