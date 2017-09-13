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
import io.viewserver.messages.protobuf.dto.TableMetaDataMessage;
import io.viewserver.messages.tableevent.ITableMetadata;

import java.util.List;

/**
 * Created by nick on 02/12/15.
 */
public class TableMetadata extends PoolableMessage<TableMetadata> implements ITableMetadata<TableMetadata> {
    private TableMetaDataMessage.TableMetaDataDtoOrBuilder tableMetadataDto;
    private RecyclingList<IMetadataValue, TableMetaDataMessage.TableMetaDataDto.MetaDataValue> metadataValuesList;

    public static TableMetadata fromDto(TableMetaDataMessage.TableMetaDataDto tableMetaDataDto) {
        final TableMetadata tableMetadata = (TableMetadata) MessagePool.getInstance().get(ITableMetadata.class);
        tableMetadata.tableMetadataDto = tableMetaDataDto;
        return tableMetadata;
    }

    TableMetadata() {
        super(ITableMetadata.class);
    }

    @Override
    public List<IMetadataValue> getMetadataValues() {
        if (metadataValuesList == null) {
            metadataValuesList = new RecyclingList<IMetadataValue, TableMetaDataMessage.TableMetaDataDto.MetaDataValue>(
                    IMetadataValue.class) {
                @Override
                protected void doAdd(Object dto) {
                    final TableMetaDataMessage.TableMetaDataDto.Builder builder = getTableMetadataDtoBuilder();
                    dtoList = builder.getMetaDataValueList();
                    if (dto instanceof TableMetaDataMessage.TableMetaDataDto.MetaDataValue) {
                        builder.addMetaDataValue((TableMetaDataMessage.TableMetaDataDto.MetaDataValue) dto);
                    } else {
                        builder.addMetaDataValue((TableMetaDataMessage.TableMetaDataDto.MetaDataValue.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getTableMetadataDtoBuilder().clearMetaDataValue();
                }
            };
        }
        metadataValuesList.setDtoList(tableMetadataDto != null ? tableMetadataDto.getMetaDataValueList() : null);
        return metadataValuesList;
    }

    @Override
    protected void doRelease() {
        if (metadataValuesList != null) {
            metadataValuesList.release();
        }
        tableMetadataDto = null;
    }

    TableMetaDataMessage.TableMetaDataDto.Builder getBuilder() {
        return getTableMetadataDtoBuilder();
    }

    private TableMetaDataMessage.TableMetaDataDto.Builder getTableMetadataDtoBuilder() {
        if (tableMetadataDto == null) {
            tableMetadataDto = TableMetaDataMessage.TableMetaDataDto.newBuilder();
        } else if (tableMetadataDto instanceof TableMetaDataMessage.TableMetaDataDto) {
            tableMetadataDto = ((TableMetaDataMessage.TableMetaDataDto) tableMetadataDto).toBuilder();
        }
        return (TableMetaDataMessage.TableMetaDataDto.Builder) tableMetadataDto;
    }

    public static class MetadataValue extends PoolableMessage<MetadataValue> implements IMetadataValue<MetadataValue> {
        private TableMetaDataMessage.TableMetaDataDto.MetaDataValueOrBuilder metadataValueDto;
        private Value value;

        MetadataValue() {
            super(IMetadataValue.class);
        }

        @Override
        public void setDto(Object dto) {
            metadataValueDto = (TableMetaDataMessage.TableMetaDataDto.MetaDataValueOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return metadataValueDto.getName();
        }

        @Override
        public IMetadataValue setName(String name) {
            getMetadataValueDtoBuilder().setName(name);
            return this;
        }

        @Override
        public IValue getValue() {
            if (value == null) {
                value = metadataValueDto != null ? Value.fromDto(metadataValueDto.getValue())
                        : (Value)MessagePool.getInstance().get(IValue.class);
            }
            return value;
        }

        @Override
        public IMetadataValue setValue(IValue value) {
            if (this.value != null) {
                this.value.release();
            }
            this.value = (Value) value.retain();
            return this;
        }

        @Override
        protected void doRelease() {
            if (value != null) {
                value.release();
                value = null;
            }
            metadataValueDto = null;
        }

        TableMetaDataMessage.TableMetaDataDto.MetaDataValue.Builder getBuilder() {
            final TableMetaDataMessage.TableMetaDataDto.MetaDataValue.Builder builder = getMetadataValueDtoBuilder();
            if (value != null) {
                builder.setValue(value.getBuilder());
            }
            return builder;
        }

        private TableMetaDataMessage.TableMetaDataDto.MetaDataValue.Builder getMetadataValueDtoBuilder() {
            if (metadataValueDto == null) {
                metadataValueDto = TableMetaDataMessage.TableMetaDataDto.MetaDataValue.newBuilder();
            } else if (metadataValueDto instanceof TableMetaDataMessage.TableMetaDataDto.MetaDataValue) {
                metadataValueDto = ((TableMetaDataMessage.TableMetaDataDto.MetaDataValue) metadataValueDto).toBuilder();
            }
            return (TableMetaDataMessage.TableMetaDataDto.MetaDataValue.Builder) metadataValueDto;
        }
    }

    public static class Value extends PoolableMessage<Value> implements IValue<Value> {
        private TableMetaDataMessage.TableMetaDataDto.ValueOrBuilder valueDto;

        public static Value fromDto(TableMetaDataMessage.TableMetaDataDto.Value dto) {
            final Value value = (Value)MessagePool.getInstance().get(IValue.class);
            value.valueDto = dto;
            return value;
        }

        Value() {
            super(IValue.class);
        }

        @Override
        public ColumnType getValueType() {
            final TableMetaDataMessage.TableMetaDataDto.Value.ValueCase valueCase = getValueCase();
            switch (valueCase) {
                case BOOLEANVALUE: {
                    return ColumnType.Boolean;
                }
                case INTVALUE: {
                    return ColumnType.Integer;
                }
                case LONGVALUE: {
                    return ColumnType.Long;
                }
                case FLOATVALUE: {
                    return ColumnType.Float;
                }
                case DOUBLEVALUE: {
                    return ColumnType.Double;
                }
                case STRINGVALUE: {
                    return ColumnType.String;
                }
                case VALUE_NOT_SET: {
                    return null;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("Unknown value type '%s'", valueCase));
                }
            }
        }

        @Override
        public boolean getBooleanValue() {
            final TableMetaDataMessage.TableMetaDataDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.BOOLEANVALUE &&
                    valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get boolean value", valueCase));
            }
            return valueDto.getBooleanValue();
        }

        @Override
        public IValue setBooleanValue(boolean value) {
            getValueDtoBuilder().setBooleanValue(value);
            return this;
        }

        @Override
        public int getIntegerValue() {
            final TableMetaDataMessage.TableMetaDataDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.INTVALUE &&
                    valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get integer value", valueCase));
            }
            return valueDto.getIntValue();
        }

        @Override
        public IValue setIntegerValue(int value) {
            getValueDtoBuilder().setIntValue(value);
            return this;
        }

        @Override
        public long getLongValue() {
            final TableMetaDataMessage.TableMetaDataDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.LONGVALUE &&
                    valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get long value", valueCase));
            }
            return valueDto.getLongValue();
        }

        @Override
        public IValue setLongValue(long value) {
            getValueDtoBuilder().setLongValue(value);
            return this;
        }

        @Override
        public float getFloatValue() {
            final TableMetaDataMessage.TableMetaDataDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.FLOATVALUE &&
                    valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get float value", valueCase));
            }
            return valueDto.getFloatValue();
        }

        @Override
        public IValue setFloatValue(float value) {
            getValueDtoBuilder().setFloatValue(value);
            return this;
        }

        @Override
        public double getDoubleValue() {
            final TableMetaDataMessage.TableMetaDataDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.DOUBLEVALUE &&
                    valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get double value", valueCase));
            }
            return valueDto.getDoubleValue();
        }

        @Override
        public IValue setDoubleValue(double value) {
            getValueDtoBuilder().setDoubleValue(value);
            return this;
        }

        @Override
        public String getStringValue() {
            final TableMetaDataMessage.TableMetaDataDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.STRINGVALUE &&
                    valueCase != TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get string value", valueCase));
            }
            return valueDto.getStringValue();
        }

        @Override
        public IValue setStringValue(String value) {
            getValueDtoBuilder().setStringValue(value);
            return this;
        }

        @Override
        protected void doRelease() {
            valueDto = null;
        }

        TableMetaDataMessage.TableMetaDataDto.Value.Builder getBuilder() {
            return getValueDtoBuilder();
        }

        private TableMetaDataMessage.TableMetaDataDto.Value.Builder getValueDtoBuilder() {
            if (valueDto == null) {
                valueDto = TableMetaDataMessage.TableMetaDataDto.Value.newBuilder();
            } else if (valueDto instanceof TableMetaDataMessage.TableMetaDataDto.Value) {
                valueDto = ((TableMetaDataMessage.TableMetaDataDto.Value) valueDto).toBuilder();
            }
            return (TableMetaDataMessage.TableMetaDataDto.Value.Builder) valueDto;
        }

        private TableMetaDataMessage.TableMetaDataDto.Value.ValueCase getValueCase() {
            // for some reason, this isn't in the ValueOrBuilder interface...
            if (valueDto == null) {
                return TableMetaDataMessage.TableMetaDataDto.Value.ValueCase.VALUE_NOT_SET;
            } else if (valueDto instanceof TableMetaDataMessage.TableMetaDataDto.Value) {
                return ((TableMetaDataMessage.TableMetaDataDto.Value) valueDto).getValueCase();
            } else {
                return ((TableMetaDataMessage.TableMetaDataDto.Value.Builder)valueDto).getValueCase();
            }
        }
    }
}
