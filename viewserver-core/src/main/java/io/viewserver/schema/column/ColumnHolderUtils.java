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
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.table.ISchemaConfig;
import io.viewserver.operators.table.TableKeyDefinition;

import java.util.Date;
import java.util.List;

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


    public static io.viewserver.schema.column.ColumnType getType(ContentType contentType) {
        switch (contentType) {
            case Bool: {
                return io.viewserver.schema.column.ColumnType.Bool;
            }
            case NullableBool: {
                return ColumnType.NullableBool;
            }
            case Byte: {
                return ColumnType.Byte;
            }
            case Short: {
                return ColumnType.Short;
            }
            case Int: {
                return ColumnType.Int;
            }
            case Long: {
                return ColumnType.Long;
            }
            case Float: {
                return ColumnType.Float;
            }
            case Double: {
                return ColumnType.Double;
            }
            case String: {
                return ColumnType.String;
            }
            default: {
                throw new IllegalArgumentException("Cannot create metadata for column of unknown type " + contentType);
            }
        }
    }

    public static void setValue(ColumnHolder columnHolder, int row, Object value) {
        if(columnHolder.getColumn() == null){
            throw new RuntimeException(String.format("No column configured for column holder named %s",columnHolder.getName()));
        }
        switch (columnHolder.getType()) {
            case Bool: {
                ((IWritableColumnBool)columnHolder.getColumn()).setBool(row, (Boolean) defaultVal(value,false) );
                break;
            }
            case NullableBool: {
                ((IWritableColumnNullableBool)columnHolder.getColumn()).setNullableBool(row, (NullableBool) value);
                break;
            }
            case Byte: {
                ((IWritableColumnByte)columnHolder.getColumn()).setByte(row, (Byte) defaultVal(value,-1) );
                break;
            }
            case Short: {
                ((IWritableColumnShort)columnHolder.getColumn()).setShort(row, (Short) defaultVal(value,-1) );
                break;
            }
            case Int: {
                ((IWritableColumnInt)columnHolder.getColumn()).setInt(row, (Integer) defaultVal(value,-1) );
                break;
            }
            case Long: {
                Object val = columnHolder.getMetadata().getDataType().getColumnType().getNullValue();
                if(value != null) {
                    switch (columnHolder.getMetadata().getDataType()) {
                        case Date:
                        case DateTime: {
                            val = ((Date) value).getTime();
                            break;
                        }
                        default: {
                            val = (Long) value;
                        }
                    }
                }
                ((IWritableColumnLong)columnHolder.getColumn()).setLong(row, (Long) defaultVal(val,Long.valueOf(-1)) );
                break;
            }
            case Float: {
                ((IWritableColumnFloat)columnHolder.getColumn()).setFloat(row, (Float) defaultVal(value,Float.valueOf(-1)) );
                break;
            }
            case Double: {
                IWritableColumnDouble column = (IWritableColumnDouble) columnHolder.getColumn();
                column.setDouble(row, (Double) defaultVal(value,Double.valueOf(-1)) );
                break;
            }
            case String: {
                ((IWritableColumnString)columnHolder.getColumn()).setString(row, (String) value);
                break;
            }
            default: {
                throw new RuntimeException("Unable to set value " + value + " of type " + columnHolder.getType());
            }
        }
    }

    private static Object defaultVal(Object value, Object defaultVal) {
        if(value == null){
            return defaultVal;
        }
        return value;
    }


    public static TableKeyDefinition getKey(ISchemaConfig config){
        List<String> keyColumns = config.getKeyColumns();
        return new TableKeyDefinition(keyColumns.toArray(new String[keyColumns.size()]));
    }

    public static io.viewserver.schema.Schema getSchema(ISchemaConfig schema1 ) {
        io.viewserver.schema.Schema schema = new io.viewserver.schema.Schema();
        List<Column> columns = schema1.getColumns();
        int count = columns.size();
        for (int i = 0; i < count; i++) {
            Column column = columns.get(i);
            ColumnHolder columnHolder = createColumnHolder(column.getName(), column.getType().getColumnType());
            ColumnMetadata columnMetadata = createColumnMetadata(columnHolder.getType());
            columnMetadata.setDataType(column.getType());
            columnHolder.setMetadata(columnMetadata);
            schema.addColumn(columnHolder);
        }
        return schema;
    }
}
