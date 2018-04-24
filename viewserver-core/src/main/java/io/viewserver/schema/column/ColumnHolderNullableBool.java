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

package io.viewserver.schema.column;

import io.viewserver.core.NullableBool;
import io.viewserver.schema.column.chunked.ChunkedColumnNullableBool;

/**
 * Created by bemm on 26/09/2014.
 */
public class ColumnHolderNullableBool extends ColumnHolder implements IColumnNullableBool {
    public ColumnHolderNullableBool(String name) {
        super(name);
    }

    public ColumnHolderNullableBool(String name, IRowMapper rowMapper) {
        super(name, rowMapper);
    }

    public ColumnHolderNullableBool(IColumn column, IRowMapper rowMapper) {
        super(column, rowMapper);
    }

    @Override
    public boolean supportsPreviousValues() {
        return ((IColumnNullableBool)getColumn()).supportsPreviousValues();
    }

    @Override
    public ColumnType getType() {
        return ColumnType.NullableBool;
    }

    @Override
    public NullableBool getNullableBool(int row) {
        int sourceRow = getSourceRow(row);
        if (sourceRow == -1) {
            return ChunkedColumnNullableBool.DEFAULT_VALUE;
        }
        return ((IColumnNullableBool)getColumn()).getNullableBool(sourceRow);
    }

    @Override
    public boolean getBool(int row) {
        return getNullableBool(row) == NullableBool.True;
    }

    @Override
    public NullableBool getPreviousNullableBool(int row) {
        int sourceRow = getSourceRow(row);
        if (sourceRow == -1) {
            return ChunkedColumnNullableBool.DEFAULT_VALUE;
        }
        return ((IColumnNullableBool)getColumn()).getPreviousNullableBool(sourceRow);
    }

    @Override
    public ColumnMetadataNullableBool getMetadata() {
        return (ColumnMetadataNullableBool)super.getMetadata();
    }
}
