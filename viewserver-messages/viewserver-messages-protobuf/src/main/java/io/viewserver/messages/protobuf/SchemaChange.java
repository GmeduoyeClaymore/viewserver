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

package io.viewserver.messages.protobuf;

import io.viewserver.messages.MessagePool;
import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.common.ColumnType;
import io.viewserver.messages.protobuf.dto.SchemaChangeMessage;
import io.viewserver.messages.tableevent.ISchemaChange;

import java.util.List;

/**
 * Created by nick on 04/12/15.
 */
public class SchemaChange extends PoolableMessage<SchemaChange> implements ISchemaChange<SchemaChange> {
    private SchemaChangeMessage.SchemaChangeDtoOrBuilder schemaChangeDto;
    private RecyclingList<IAddColumn, SchemaChangeMessage.SchemaChangeDto.AddColumn> addColumnsList;
    private RecyclingList<IRemoveColumn, SchemaChangeMessage.SchemaChangeDto.RemoveColumn> removeColumnsList;

    public static SchemaChange fromDto(SchemaChangeMessage.SchemaChangeDto schemaChangeDto) {
        final SchemaChange schemaChange = (SchemaChange) MessagePool.getInstance().get(ISchemaChange.class);
        schemaChange.schemaChangeDto = schemaChangeDto;
        return schemaChange;
    }

    SchemaChange() {
        super(ISchemaChange.class);
    }

    @Override
    public int getSchemaSize() {
        return schemaChangeDto.getSchemaSize();
    }

    @Override
    public ISchemaChange setSchemaSize(int schemaSize) {
        getSchemaChangeDtoBuilder().setSchemaSize(schemaSize);
        return this;
    }

    @Override
    public List<IAddColumn> getAddColumns() {
        if (addColumnsList == null) {
            addColumnsList = new RecyclingList<IAddColumn, SchemaChangeMessage.SchemaChangeDto.AddColumn>(
                    IAddColumn.class) {
                @Override
                protected void doAdd(Object dto) {
                    final SchemaChangeMessage.SchemaChangeDto.Builder builder = getSchemaChangeDtoBuilder();
                    dtoList = builder.getAddedColumnsList();
                    if (dto instanceof SchemaChangeMessage.SchemaChangeDto.AddColumn) {
                        builder.addAddedColumns((SchemaChangeMessage.SchemaChangeDto.AddColumn) dto);
                    } else {
                        builder.addAddedColumns((SchemaChangeMessage.SchemaChangeDto.AddColumn.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getSchemaChangeDtoBuilder().clearAddedColumns();
                }
            };
        }
        addColumnsList.setDtoList(schemaChangeDto != null ? schemaChangeDto.getAddedColumnsList() : null);
        return addColumnsList;
    }

    @Override
    public List<IRemoveColumn> getRemoveColumns() {
        if (removeColumnsList == null) {
            removeColumnsList = new RecyclingList<IRemoveColumn, SchemaChangeMessage.SchemaChangeDto.RemoveColumn>(
                    IRemoveColumn.class) {
                @Override
                protected void doAdd(Object dto) {
                    final SchemaChangeMessage.SchemaChangeDto.Builder builder = getSchemaChangeDtoBuilder();
                    dtoList = builder.getRemovedColumnsList();
                    if (dto instanceof SchemaChangeMessage.SchemaChangeDto.RemoveColumn) {
                        builder.addRemovedColumns((SchemaChangeMessage.SchemaChangeDto.RemoveColumn) dto);
                    } else {
                        builder.addRemovedColumns((SchemaChangeMessage.SchemaChangeDto.RemoveColumn.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getSchemaChangeDtoBuilder().clearRemovedColumns();
                }
            };
        }
        removeColumnsList.setDtoList(schemaChangeDto != null ? schemaChangeDto.getRemovedColumnsList() : null);
        return removeColumnsList;
    }

    @Override
    protected void doRelease() {
        if (addColumnsList != null) {
            addColumnsList.release();
        }
        if (removeColumnsList != null) {
            removeColumnsList.release();
        }
        schemaChangeDto = null;
    }

    SchemaChangeMessage.SchemaChangeDto.Builder getBuilder() {
        return getSchemaChangeDtoBuilder();
    }

    private SchemaChangeMessage.SchemaChangeDto.Builder getSchemaChangeDtoBuilder() {
        if (schemaChangeDto == null) {
            schemaChangeDto = SchemaChangeMessage.SchemaChangeDto.newBuilder();
        } else if (schemaChangeDto instanceof SchemaChangeMessage.SchemaChangeDto) {
            schemaChangeDto = ((SchemaChangeMessage.SchemaChangeDto) schemaChangeDto).toBuilder();
        }
        return (SchemaChangeMessage.SchemaChangeDto.Builder) schemaChangeDto;
    }

    public static class AddColumn extends PoolableMessage<AddColumn> implements IAddColumn<AddColumn> {
        private SchemaChangeMessage.SchemaChangeDto.AddColumnOrBuilder addColumnDto;

        public AddColumn() {
            super(IAddColumn.class);
        }

        @Override
        public void setDto(Object dto) {
            addColumnDto = (SchemaChangeMessage.SchemaChangeDto.AddColumnOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        public int getColumnId() {
            return addColumnDto.getColumnId();
        }

        @Override
        public IAddColumn setColumnId(int columnId) {
            getAddColumnDtoBuilder().setColumnId(columnId);
            return this;
        }

        @Override
        public ColumnType getType() {
            final SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType type = addColumnDto.getType();
            switch (type) {
                case BOOLEAN: {
                    return ColumnType.Boolean;
                }
                case NULLABLEBOOLEAN: {
                    return ColumnType.NullableBoolean;
                }
                case BYTE: {
                    return ColumnType.Byte;
                }
                case SHORT: {
                    return ColumnType.Short;
                }
                case INTEGER: {
                    return ColumnType.Integer;
                }
                case LONG: {
                    return ColumnType.Long;
                }
                case FLOAT: {
                    return ColumnType.Float;
                }
                case DOUBLE: {
                    return ColumnType.Double;
                }
                case STRING: {
                    return ColumnType.String;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("Unknown column type '%s'", type));
                }
            }
        }

        @Override
        public IAddColumn setType(ColumnType type) {
            switch (type) {
                case Boolean: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.BOOLEAN);
                    break;
                }
                case NullableBoolean: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.NULLABLEBOOLEAN);
                    break;
                }
                case Byte: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.BYTE);
                    break;
                }
                case Short: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.SHORT);
                    break;
                }
                case Integer: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.INTEGER);
                    break;
                }
                case Long: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.LONG);
                    break;
                }
                case Float: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.FLOAT);
                    break;
                }
                case Double: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.DOUBLE);
                    break;
                }
                case String: {
                    getAddColumnDtoBuilder().setType(SchemaChangeMessage.SchemaChangeDto.AddColumn.ColumnType.STRING);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Unknown column type '%s'", type));
                }
            }
            return this;
        }

        @Override
        public String getName() {
            return addColumnDto.getName();
        }

        @Override
        public IAddColumn setName(String name) {
            getAddColumnDtoBuilder().setName(name);
            return this;
        }

        @Override
        public DataType getDataType() {
            final SchemaChangeMessage.DataType dataType = addColumnDto.getDataType();
            switch (dataType) {
                case BOOLEAN: {
                    return DataType.Boolean;
                }
                case NULLABLEBOOLEAN: {
                    return DataType.NullableBoolean;
                }
                case BYTE: {
                    return DataType.Byte;
                }
                case SHORT: {
                    return DataType.Short;
                }
                case INTEGER: {
                    return DataType.Integer;
                }
                case LONG: {
                    return DataType.Long;
                }
                case FLOAT: {
                    return DataType.Float;
                }
                case DOUBLE: {
                    return DataType.Double;
                }
                case STRING: {
                    return DataType.String;
                }
                case DATE: {
                    return DataType.Date;
                }
                case DATETIME: {
                    return DataType.DateTime;
                }
                case JSON: {
                    return DataType.Json;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("Unknown column type '%s'", dataType));
                }
            }
        }

        @Override
        public IAddColumn setDataType(DataType dataType) {
            switch (dataType) {
                case Boolean: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.BOOLEAN);
                    break;
                }
                case NullableBoolean: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.NULLABLEBOOLEAN);
                    break;
                }
                case Byte: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.BYTE);
                    break;
                }
                case Short: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.SHORT);
                    break;
                }
                case Integer: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.INTEGER);
                    break;
                }
                case Long: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.LONG);
                    break;
                }
                case Float: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.FLOAT);
                    break;
                }
                case Double: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.DOUBLE);
                    break;
                }
                case String: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.STRING);
                    break;
                }
                case Date: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.DATE);
                    break;
                }
                case DateTime: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.DATETIME);
                    break;
                }
                case Json: {
                    getAddColumnDtoBuilder().setDataType(SchemaChangeMessage.DataType.JSON);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Unknown column type '%s'", dataType));
                }
            }
            return this;
        }

        @Override
        public boolean getBooleanNullValue() {
            return addColumnDto.getBooleanNullValue();
        }

        @Override
        public IAddColumn setBooleanNullValue(boolean nullValue) {
            getAddColumnDtoBuilder().setBooleanNullValue(nullValue);
            return this;
        }

        @Override
        public int getIntegerNullValue() {
            return addColumnDto.getIntNullValue();
        }

        @Override
        public IAddColumn setIntegerNullValue(int nullValue) {
            getAddColumnDtoBuilder().setIntNullValue(nullValue);
            return this;
        }

        @Override
        public long getLongNullValue() {
            return addColumnDto.getLongNullValue();
        }

        @Override
        public IAddColumn setLongNullValue(long nullValue) {
            getAddColumnDtoBuilder().setLongNullValue(nullValue);
            return this;
        }

        @Override
        public float getFloatNullValue() {
            return addColumnDto.getFloatNullValue();
        }

        @Override
        public IAddColumn setFloatNullValue(float nullValue) {
            getAddColumnDtoBuilder().setFloatNullValue(nullValue);
            return this;
        }

        @Override
        public double getDoubleNullValue() {
            return addColumnDto.getDoubleNullValue();
        }

        @Override
        public IAddColumn setDoubleNullValue(double nullValue) {
            getAddColumnDtoBuilder().setDoubleNullValue(nullValue);
            return this;
        }

        @Override
        public String getStringNullValue() {
            return addColumnDto.getStringNullValue();
        }

        @Override
        public IAddColumn setStringNullValue(String nullValue) {
            getAddColumnDtoBuilder().setStringNullValue(nullValue);
            return this;
        }

        @Override
        public boolean getNullNullValue() {
            return addColumnDto.hasNullNullValue();
        }

        @Override
        public IAddColumn setNullNullValue() {
            getAddColumnDtoBuilder().setNullNullValue(1);
            return this;
        }

        @Override
        protected void doRelease() {
            addColumnDto = null;
        }

        SchemaChangeMessage.SchemaChangeDto.AddColumn.Builder getBuilder() {
            return getAddColumnDtoBuilder();
        }

        private SchemaChangeMessage.SchemaChangeDto.AddColumn.Builder getAddColumnDtoBuilder() {
            if (addColumnDto == null) {
                addColumnDto = SchemaChangeMessage.SchemaChangeDto.AddColumn.newBuilder();
            } else if (addColumnDto instanceof SchemaChangeMessage.SchemaChangeDto.AddColumn) {
                addColumnDto = ((SchemaChangeMessage.SchemaChangeDto.AddColumn) addColumnDto).toBuilder();
            }
            return (SchemaChangeMessage.SchemaChangeDto.AddColumn.Builder) addColumnDto;
        }
    }

    public static class RemoveColumn extends PoolableMessage<RemoveColumn> implements IRemoveColumn<RemoveColumn> {
        private SchemaChangeMessage.SchemaChangeDto.RemoveColumnOrBuilder removeColumnDto;

        public RemoveColumn() {
            super(IRemoveColumn.class);
        }

        @Override
        public void setDto(Object dto) {
            removeColumnDto = (SchemaChangeMessage.SchemaChangeDto.RemoveColumnOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public int getColumnId() {
            return removeColumnDto.getColumnId();
        }

        @Override
        public IRemoveColumn setColumnId(int columnId) {
            getRemoveColumnDtoBuilder().setColumnId(columnId);
            return this;
        }

        @Override
        protected void doRelease() {
            removeColumnDto = null;
        }

        SchemaChangeMessage.SchemaChangeDto.RemoveColumn.Builder getBuilder() {
            return getRemoveColumnDtoBuilder();
        }

        private SchemaChangeMessage.SchemaChangeDto.RemoveColumn.Builder getRemoveColumnDtoBuilder() {
            if (removeColumnDto == null) {
                removeColumnDto = SchemaChangeMessage.SchemaChangeDto.RemoveColumn.newBuilder();
            } else if (removeColumnDto instanceof SchemaChangeMessage.SchemaChangeDto.RemoveColumn) {
                removeColumnDto = ((SchemaChangeMessage.SchemaChangeDto.RemoveColumn) removeColumnDto).toBuilder();
            }
            return (SchemaChangeMessage.SchemaChangeDto.RemoveColumn.Builder) removeColumnDto;
        }
    }
}
