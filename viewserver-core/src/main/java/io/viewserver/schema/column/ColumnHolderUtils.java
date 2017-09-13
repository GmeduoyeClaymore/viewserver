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

/**
 * Created by nickc on 26/09/2014.
 */
public class ColumnHolderUtils {
    public static ColumnHolder createColumnHolder(String name, ColumnType type) {
        return createColumnHolder(name, type, null);
    }

    public static ColumnHolder createColumnHolder(String name, ColumnType type, IRowMapper rowMapper) {
        switch (type) {
            case Bool: {
                return new ColumnHolderBool(name, rowMapper);
            }
            case NullableBool: {
                return new ColumnHolderNullableBool(name, rowMapper);
            }
            case Byte: {
                return new ColumnHolderByte(name, rowMapper);
            }
            case Short: {
                return new ColumnHolderShort(name, rowMapper);
            }
            case Int: {
                return new ColumnHolderInt(name, rowMapper);
            }
            case Long: {
                return new ColumnHolderLong(name, rowMapper);
            }
            case Float: {
                return new ColumnHolderFloat(name, rowMapper);
            }
            case Double: {
                return new ColumnHolderDouble(name, rowMapper);
            }
            case String: {
                return new ColumnHolderString(name, rowMapper);
            }
            default: {
                return null;
            }
        }
    }

    public static ColumnHolder createColumnHolder(IColumn column) {
        return createColumnHolder(column, null);
    }

    public static ColumnHolder createColumnHolder(IColumn column, IRowMapper rowMapper) {
        switch (column.getType()) {
            case Bool: {
                return new ColumnHolderBool(column, rowMapper);
            }
            case NullableBool: {
                return new ColumnHolderNullableBool(column, rowMapper);
            }
            case Byte: {
                return new ColumnHolderByte(column, rowMapper);
            }
            case Short: {
                return new ColumnHolderShort(column, rowMapper);
            }
            case Int: {
                return new ColumnHolderInt(column, rowMapper);
            }
            case Long: {
                return new ColumnHolderLong(column, rowMapper);
            }
            case Float: {
                return new ColumnHolderFloat(column, rowMapper);
            }
            case Double: {
                return new ColumnHolderDouble(column, rowMapper);
            }
            case String: {
                return new ColumnHolderString(column, rowMapper);
            }
            default: {
                return null;
            }
        }
    }

    public static Object getValue(ColumnHolder columnHolder, int row) {
        switch (columnHolder.getType()) {
            case Bool: {
                return ((IColumnBool)columnHolder).getBool(row);
            }
            case NullableBool: {
                return ((IColumnNullableBool)columnHolder).getNullableBool(row);
            }
            case Byte: {
                return ((IColumnByte)columnHolder).getByte(row);
            }
            case Short: {
                return ((IColumnShort)columnHolder).getShort(row);
            }
            case Int: {
                return ((IColumnInt)columnHolder).getInt(row);
            }
            case Long: {
                return ((IColumnLong)columnHolder).getLong(row);
            }
            case Float: {
                return ((IColumnFloat)columnHolder).getFloat(row);
            }
            case Double: {
                return ((IColumnDouble)columnHolder).getDouble(row);
            }
            case String: {
                return ((IColumnString)columnHolder).getString(row);
            }
            default: {
                return "?";
            }
        }
    }

    public static Object getPreviousValue(ColumnHolder columnHolder, int row) {
        switch (columnHolder.getType()) {
            case Bool: {
                return ((IColumnBool)columnHolder).getPreviousBool(row);
            }
            case NullableBool: {
                return ((IColumnNullableBool)columnHolder).getPreviousNullableBool(row);
            }
            case Byte: {
                return ((IColumnByte)columnHolder).getPreviousByte(row);
            }
            case Short: {
                return ((IColumnShort)columnHolder).getPreviousShort(row);
            }
            case Int: {
                return ((IColumnInt)columnHolder).getPreviousInt(row);
            }
            case Long: {
                return ((IColumnLong)columnHolder).getPreviousLong(row);
            }
            case Float: {
                return ((IColumnFloat)columnHolder).getPreviousFloat(row);
            }
            case Double: {
                return ((IColumnDouble)columnHolder).getPreviousDouble(row);
            }
            case String: {
                return ((IColumnString)columnHolder).getPreviousString(row);
            }
            default: {
                return "?";
            }
        }
    }

    public static ColumnMetadata createColumnMetadata(ColumnType columnType) {
        switch (columnType) {
            case Bool: {
                return new ColumnMetadataBool();
            }
            case NullableBool: {
                return new ColumnMetadataNullableBool();
            }
            case Byte: {
                return new ColumnMetadataByte();
            }
            case Short: {
                return new ColumnMetadataShort();
            }
            case Int: {
                return new ColumnMetadataInt();
            }
            case Long: {
                return new ColumnMetadataLong();
            }
            case Float: {
                return new ColumnMetadataFloat();
            }
            case Double: {
                return new ColumnMetadataDouble();
            }
            case String: {
                return new ColumnMetadataString();
            }
            default: {
                throw new IllegalArgumentException("Cannot create metadata for column of unknown type " + columnType);
            }
        }
    }
}
