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

import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.common.ColumnType;
import io.viewserver.messages.protobuf.dto.RowEventMessage;
import io.viewserver.messages.tableevent.IRowEvent;

import java.util.List;

/**
 * Created by nick on 07/12/15.
 */
public class RowEvent extends PoolableMessage<RowEvent> implements IRowEvent<RowEvent> {
    private RowEventMessage.RowEventDtoOrBuilder rowEventDto;
    private RecyclingList<IColumnValue, RowEventMessage.RowEventDto.ColumnValue> columnValues;

    RowEvent() {
        super(IRowEvent.class);
    }

    @Override
    public void setDto(Object dto) {
        rowEventDto = (RowEventMessage.RowEventDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public Type getType() {
        final RowEventMessage.RowEventDto.RowEventType eventType = rowEventDto.getEventType();
        switch (eventType) {
            case ADD: {
                return Type.Add;
            }
            case UPDATE: {
                return Type.Update;
            }
            case REMOVE: {
                return Type.Remove;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown row event type '%s'", eventType));
            }
        }
    }

    @Override
    public IRowEvent setType(Type type) {
        switch (type) {
            case Add: {
                getRowEventDtoBuilder().setEventType(RowEventMessage.RowEventDto.RowEventType.ADD);
                break;
            }
            case Update: {
                getRowEventDtoBuilder().setEventType(RowEventMessage.RowEventDto.RowEventType.UPDATE);
                break;
            }
            case Remove: {
                getRowEventDtoBuilder().setEventType(RowEventMessage.RowEventDto.RowEventType.REMOVE);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown row event type '%s'", type));
            }
        }
        return this;
    }

    @Override
    public int getRowId() {
        return rowEventDto.getRowId();
    }

    @Override
    public IRowEvent setRowId(int rowId) {
        getRowEventDtoBuilder().setRowId(rowId);
        return this;
    }

    @Override
    public List<IColumnValue> getColumnValues() {
        if (columnValues == null) {
            columnValues = new RecyclingList<IColumnValue, RowEventMessage.RowEventDto.ColumnValue>(
                    IColumnValue.class
            ) {
                @Override
                protected void doAdd(Object dto) {
                    final RowEventMessage.RowEventDto.Builder builder = getRowEventDtoBuilder();
                    dtoList = builder.getValuesList();
                    if (dto instanceof RowEventMessage.RowEventDto.ColumnValue) {
                        builder.addValues((RowEventMessage.RowEventDto.ColumnValue) dto);
                    } else {
                        builder.addValues((RowEventMessage.RowEventDto.ColumnValue.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getRowEventDtoBuilder().clearValues();
                }
            };
        }
        columnValues.setDtoList(rowEventDto != null ? rowEventDto.getValuesList() : null);
        return columnValues;
    }

    @Override
    protected void doRelease() {
        if (columnValues != null) {
            columnValues.release();
        }
        rowEventDto = null;
    }

    RowEventMessage.RowEventDto.Builder getBuilder() {
        return getRowEventDtoBuilder();
    }

    private RowEventMessage.RowEventDto.Builder getRowEventDtoBuilder() {
        if (rowEventDto == null) {
            rowEventDto = RowEventMessage.RowEventDto.newBuilder();
        } else if (rowEventDto instanceof RowEventMessage.RowEventDto) {
            rowEventDto = ((RowEventMessage.RowEventDto) rowEventDto).toBuilder();
        }
        return (RowEventMessage.RowEventDto.Builder) rowEventDto;
    }

    public static class ColumnValue extends PoolableMessage<ColumnValue> implements IRowEvent.IColumnValue<ColumnValue> {
        private RowEventMessage.RowEventDto.ColumnValueOrBuilder columnValueDto;

        ColumnValue() {
            super(IRowEvent.IColumnValue.class);
        }

        @Override
        public void setDto(Object dto) {
            columnValueDto = (RowEventMessage.RowEventDto.ColumnValueOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public int getColumnId() {
            return columnValueDto.getColumnId();
        }

        @Override
        public IRowEvent.IColumnValue setColumnId(int columnId) {
            getColumnValueDtoBuilder().setColumnId(columnId);
            return this;
        }

        @Override
        public ColumnType getValueType() {
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
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
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
            if (valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.BOOLEANVALUE &&
                    valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get boolean value", valueCase));
            }
            return columnValueDto.getBooleanValue();
        }

        @Override
        public IRowEvent.IColumnValue setBooleanValue(boolean value) {
            getColumnValueDtoBuilder().setBooleanValue(value);
            return this;
        }

        @Override
        public int getIntegerValue() {
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
            if (valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.INTVALUE &&
                    valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get integer value", valueCase));
            }
            return columnValueDto.getIntValue();
        }

        @Override
        public IRowEvent.IColumnValue setIntegerValue(int value) {
            getColumnValueDtoBuilder().setIntValue(value);
            return this;
        }

        @Override
        public long getLongValue() {
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
            if (valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.LONGVALUE &&
                    valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get long value", valueCase));
            }
            return columnValueDto.getLongValue();
        }

        @Override
        public IRowEvent.IColumnValue setLongValue(long value) {
            getColumnValueDtoBuilder().setLongValue(value);
            return this;
        }

        @Override
        public float getFloatValue() {
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
            if (valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.FLOATVALUE &&
                    valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get float value", valueCase));
            }
            return columnValueDto.getFloatValue();
        }

        @Override
        public IRowEvent.IColumnValue setFloatValue(float value) {
            getColumnValueDtoBuilder().setFloatValue(value);
            return this;
        }

        @Override
        public double getDoubleValue() {
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
            if (valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.DOUBLEVALUE &&
                    valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get double value", valueCase));
            }
            return columnValueDto.getDoubleValue();
        }

        @Override
        public IRowEvent.IColumnValue setDoubleValue(double value) {
            getColumnValueDtoBuilder().setDoubleValue(value);
            return this;
        }

        @Override
        public String getStringValue() {
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
            if (valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.STRINGVALUE &&
                    valueCase != RowEventMessage.RowEventDto.ColumnValue.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get string value", valueCase));
            }
            return columnValueDto.getStringValue();
        }

        @Override
        public IRowEvent.IColumnValue setStringValue(String value) {
            getColumnValueDtoBuilder().setStringValue(value);
            return this;
        }

        @Override
        public boolean getNullValue() {
            final RowEventMessage.RowEventDto.ColumnValue.ValueCase valueCase = getValueCase();
            return valueCase == RowEventMessage.RowEventDto.ColumnValue.ValueCase.NULLVALUE;
        }

        @Override
        public IRowEvent.IColumnValue setNullValue() {
            getColumnValueDtoBuilder().setNullValue(1);
            return this;
        }

        @Override
        protected void doRelease() {
            columnValueDto = null;
        }

        RowEventMessage.RowEventDto.ColumnValue.Builder getBuilder() {
            return getColumnValueDtoBuilder();
        }

        private RowEventMessage.RowEventDto.ColumnValue.Builder getColumnValueDtoBuilder() {
            if (columnValueDto == null) {
                columnValueDto = RowEventMessage.RowEventDto.ColumnValue.newBuilder();
            } else if (columnValueDto instanceof RowEventMessage.RowEventDto.ColumnValue) {
                columnValueDto = ((RowEventMessage.RowEventDto.ColumnValue) columnValueDto).toBuilder();
            }
            return (RowEventMessage.RowEventDto.ColumnValue.Builder) columnValueDto;
        }

        private RowEventMessage.RowEventDto.ColumnValue.ValueCase getValueCase() {
            // for some reason, this isn't in the ValueOrBuilder interface...
            if (columnValueDto == null) {
                return RowEventMessage.RowEventDto.ColumnValue.ValueCase.VALUE_NOT_SET;
            } else if (columnValueDto instanceof RowEventMessage.RowEventDto.ColumnValue) {
                return ((RowEventMessage.RowEventDto.ColumnValue) columnValueDto).getValueCase();
            } else {
                return ((RowEventMessage.RowEventDto.ColumnValue.Builder)columnValueDto).getValueCase();
            }
        }
    }
}
