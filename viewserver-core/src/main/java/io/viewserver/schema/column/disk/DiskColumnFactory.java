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

import io.viewserver.schema.column.ColumnFactoryBase;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IColumn;
import io.viewserver.schema.column.IColumnWatcher;

/**
 * Created by nickc on 26/09/2014.
 */
public class DiskColumnFactory extends ColumnFactoryBase {
    @Override
    public void initialise(int capacity) {
    }

    @Override
    public IColumn createColumn(ColumnHolder columnHolder, IColumnWatcher columnWatcher) {
        switch (columnHolder.getType()) {
//            case Bool: {
//                return new DiskColumnBool(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
//            }
            case Byte: {
                return new DiskColumnByte(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
            }
            case Short: {
                return new DiskColumnShort(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
            }
            case Int: {
                return new DiskColumnInt(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
            }
            case Long: {
                return new DiskColumnLong(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
            }
            case Float: {
                return new DiskColumnFloat(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
            }
            case Double: {
                return new DiskColumnDouble(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
            }
//            case String: {
//                return new DiskColumnString(columnHolder, columnWatcher, ((DiskColumnStorage)getTableStorage()).getSchemaDirectory());
//            }
            default: {
                throw new IllegalArgumentException("Cannot create disk column for type '" + columnHolder.getType() + "'");
            }
        }
    }
}
