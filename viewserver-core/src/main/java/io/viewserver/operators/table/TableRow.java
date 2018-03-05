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

package io.viewserver.operators.table;

import io.viewserver.core.NullableBool;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;

/**
 * Created by nickc on 29/09/2014.
 */
public class TableRow implements ITableRow {
    private int rowId;
    private Schema schema;

    public TableRow(int rowId, Schema schema) {
        this.rowId = rowId;
        this.schema = schema;
    }

    @Override
    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    @Override
    public boolean getBool(String name) {
        return getColumn(name, ColumnType.Bool, (IColumnBool) null).getBool(rowId);
    }

    @Override
    public void setBool(String name, boolean value) {
        getColumn(name, ColumnType.Bool, (IWritableColumnBool) null).setBool(rowId, value);
    }

    @Override
    public NullableBool getNullableBool(String name) {
        return getColumn(name, ColumnType.NullableBool, (IColumnNullableBool)null).getNullableBool(rowId);
    }

    @Override
    public void setNullableBool(String name, NullableBool value) {
        getColumn(name, ColumnType.NullableBool, (IWritableColumnNullableBool)null).setNullableBool(rowId, value);
    }

    @Override
    public byte getByte(String name) {
        return getColumn(name, ColumnType.Byte, (IColumnByte) null).getByte(rowId);
    }

    @Override
    public void setByte(String name, byte value) {
        getColumn(name, ColumnType.Byte, (IWritableColumnByte) null).setByte(rowId, value);
    }

    @Override
    public short getShort(String name) {
        return getColumn(name, ColumnType.Short, (IColumnShort) null).getShort(rowId);
    }

    @Override
    public void setShort(String name, short value) {
        getColumn(name, ColumnType.Short, (IWritableColumnShort) null).setShort(rowId, value);
    }

    @Override
    public int getInt(String name) {
        return getColumn(name, ColumnType.Int, (IColumnInt) null).getInt(rowId);
    }

    @Override
    public void setInt(String name, int value) {
        getColumn(name, ColumnType.Int, (IWritableColumnInt) null).setInt(rowId, value);
    }

    @Override
    public long getLong(String name) {
        return getColumn(name, ColumnType.Long, (IColumnLong) null).getLong(rowId);
    }

    @Override
    public void setLong(String name, long value) {
        getColumn(name, ColumnType.Long, (IWritableColumnLong) null).setLong(rowId, value);
    }

    @Override
    public float getFloat(String name) {
        return getColumn(name, ColumnType.Float, (IColumnFloat) null).getFloat(rowId);
    }

    @Override
    public void setFloat(String name, float value) {
        getColumn(name, ColumnType.Float, (IWritableColumnFloat) null).setFloat(rowId, value);
    }

    @Override
    public double getDouble(String name) {
        return getColumn(name, ColumnType.Double, (IColumnDouble) null).getDouble(rowId);
    }

    @Override
    public void setDouble(String name, double value) {
        getColumn(name, ColumnType.Double, (IWritableColumnDouble) null).setDouble(rowId, value);
    }

    @Override
    public String getString(String name) {
        return getColumn(name, ColumnType.String, (IColumnString) null).getString(rowId);
    }

    @Override
    public void setString(String name, String value) {
        IWritableColumnString column = getColumn(name, ColumnType.String, (IWritableColumnString) null);
        column.setString(rowId, value);
    }

    @Override
    public Object getValue(String name) {
        return ColumnHolderUtils.getValue(schema.getColumnHolder(name), rowId);
    }

    private <T extends IColumn> T getColumn(String name, ColumnType columnType, T dummy) {
        ColumnHolder columnHolder = schema.getColumnHolder(name);
        if (columnHolder == null) {
            throw new IllegalArgumentException("Column " + name + " does not exist");
        }
        if (columnHolder.getType() != columnType) {
            throw new RuntimeException("Column " + name + ": Cannot set that value to a column of type " + columnHolder.getType());
        }
        //noinspection unchecked
        return (T)columnHolder.getColumn();
    }
}
