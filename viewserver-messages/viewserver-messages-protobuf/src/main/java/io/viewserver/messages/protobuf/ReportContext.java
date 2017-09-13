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
import io.viewserver.messages.command.IReportContext;
import io.viewserver.messages.common.ColumnType;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.messages.protobuf.dto.ReportContextMessage;

import java.util.List;

/**
 * Created by nick on 03/12/15.
 */
public class ReportContext extends PoolableMessage<ReportContext>
        implements IReportContext<ReportContext> {
    private ReportContextMessage.ReportContextDtoOrBuilder reportContextDto;
    private RecyclingList<IParameterValue, ReportContextMessage.ReportContextDto.ParameterValue> parameterValuesList;
    private RecyclingList<IDimensionValue, ReportContextMessage.ReportContextDto.Dimension> dimensionValuesList;
    private RecyclingList<IReportContext, ReportContextMessage.ReportContextDto> childContextsList;
    private RecyclingList<IDimensionValue, ReportContextMessage.ReportContextDto.Dimension> excludedFiltersList;

    public static ReportContext fromDto(ReportContextMessage.ReportContextDto reportContextDto) {
        final ReportContext reportContext = (ReportContext) MessagePool.getInstance().get(IReportContext.class);
        reportContext.reportContextDto = reportContextDto;
        return reportContext;
    }

    ReportContext() {
        super(IReportContext.class);
    }

    @Override
    public void setDto(Object dto) {
        reportContextDto = (ReportContextMessage.ReportContextDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return reportContextDto;
    }

    @Override
    public String getReportId() {
        return reportContextDto.hasReportId() ? reportContextDto.getReportId() : null;
    }

    @Override
    public IReportContext<ReportContext> setReportId(String reportId) {
        getReportContextDtoBuilder().setReportId(reportId);
        return this;
    }

    @Override
    public List<IParameterValue> getParameterValues() {
        if (parameterValuesList == null) {
            parameterValuesList = new RecyclingList<IParameterValue, ReportContextMessage.ReportContextDto.ParameterValue>(
                    IParameterValue.class) {
                @Override
                protected void doAdd(Object dto) {
                    final ReportContextMessage.ReportContextDto.Builder builder = getReportContextDtoBuilder();
                    dtoList = builder.getParametersList();
                    if (dto instanceof ReportContextMessage.ReportContextDto.ParameterValue) {
                        builder.addParameters((ReportContextMessage.ReportContextDto.ParameterValue) dto);
                    } else {
                        builder.addParameters((ReportContextMessage.ReportContextDto.ParameterValue.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getReportContextDtoBuilder().clearParameters();
                }
            };
        }
        parameterValuesList.setDtoList(reportContextDto != null ? reportContextDto.getParametersList() : null);
        return parameterValuesList;
    }

    @Override
    public List<IDimensionValue> getDimensionValues() {
        if (dimensionValuesList == null) {
            dimensionValuesList = new RecyclingList<IDimensionValue, ReportContextMessage.ReportContextDto.Dimension>(
                    IDimensionValue.class) {
                @Override
                protected void doAdd(Object dto) {
                    final ReportContextMessage.ReportContextDto.Builder builder = getReportContextDtoBuilder();
                    dtoList = builder.getDimensionsList();
                    if (dto instanceof ReportContextMessage.ReportContextDto.Dimension) {
                        builder.addDimensions((ReportContextMessage.ReportContextDto.Dimension) dto);
                    } else {
                        builder.addDimensions((ReportContextMessage.ReportContextDto.Dimension.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getReportContextDtoBuilder().clearDimensions();
                }
            };
        }
        dimensionValuesList.setDtoList(reportContextDto != null ? reportContextDto.getDimensionsList() : null);
        return dimensionValuesList;
    }

    @Override
    public List<IReportContext> getChildContexts() {
        if (childContextsList == null) {
            childContextsList = new RecyclingList<IReportContext, ReportContextMessage.ReportContextDto>(
                    IReportContext.class
            ) {
                @Override
                protected void doAdd(Object dto) {
                    final ReportContextMessage.ReportContextDto.Builder builder = getReportContextDtoBuilder();
                    dtoList = builder.getChildContextsList();
                    if (dto instanceof ReportContextMessage.ReportContextDto) {
                        builder.addChildContexts((ReportContextMessage.ReportContextDto) dto);
                    } else {
                        builder.addChildContexts((ReportContextMessage.ReportContextDto.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getReportContextDtoBuilder().clearChildContexts();
                }
            };
        }
        childContextsList.setDtoList(reportContextDto != null ? reportContextDto.getChildContextsList() : null);
        return childContextsList;
    }

    @Override
    public String getOutput() {
        return reportContextDto.hasOutput() ? reportContextDto.getOutput() : null;
    }

    @Override
    public IReportContext<ReportContext> setOutput(String output) {
        getReportContextDtoBuilder().setOutput(output);
        return this;
    }

    @Override
    public List<IDimensionValue> getExcludedFilters() {
        if (excludedFiltersList == null) {
            excludedFiltersList = new RecyclingList<IDimensionValue, ReportContextMessage.ReportContextDto.Dimension>(
                    IDimensionValue.class) {
                @Override
                protected void doAdd(Object dto) {
                    final ReportContextMessage.ReportContextDto.Builder builder = getReportContextDtoBuilder();
                    dtoList = builder.getExcludedFiltersList();
                    if (dto instanceof ReportContextMessage.ReportContextDto.Dimension) {
                        builder.addExcludedFilters((ReportContextMessage.ReportContextDto.Dimension) dto);
                    } else {
                        builder.addExcludedFilters((ReportContextMessage.ReportContextDto.Dimension.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getReportContextDtoBuilder().clearExcludedFilters();
                }
            };
        }
        excludedFiltersList.setDtoList(reportContextDto != null ? reportContextDto.getExcludedFiltersList() : null);
        return excludedFiltersList;
    }

    @Override
    public String getMultiContextMode() {
        return reportContextDto.hasMultiContextMode() ? reportContextDto.getMultiContextMode() : null;
    }

    @Override
    public IReportContext<ReportContext> setMultiContextMode(String multiContextMode) {
        getReportContextDtoBuilder().setMultiContextMode(multiContextMode);
        return this;
    }

    @Override
    protected void doRelease() {
        if (parameterValuesList != null) {
            parameterValuesList.release();
        }
        if (dimensionValuesList != null) {
            dimensionValuesList.release();
        }
        if (childContextsList != null) {
            childContextsList.release();
        }
        reportContextDto = null;
    }

    private ReportContextMessage.ReportContextDto.Builder getReportContextDtoBuilder() {
        if (reportContextDto == null) {
            reportContextDto = ReportContextMessage.ReportContextDto.newBuilder();
        } else if (reportContextDto instanceof ReportContextMessage.ReportContextDto) {
            reportContextDto = ((ReportContextMessage.ReportContextDto) reportContextDto).toBuilder();
        }
        return (ReportContextMessage.ReportContextDto.Builder) reportContextDto;
    }

    ReportContextMessage.ReportContextDto.Builder getBuilder() {
        return getReportContextDtoBuilder();
    }

    public static class ParameterValue extends PoolableMessage<ParameterValue> implements IParameterValue<ParameterValue> {
        private ReportContextMessage.ReportContextDto.ParameterValueOrBuilder parameterValueDto;
        private Value value;

        ParameterValue() {
            super(IParameterValue.class);
        }

        @Override
        public void setDto(Object dto) {
            parameterValueDto = (ReportContextMessage.ReportContextDto.ParameterValue) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        ReportContextMessage.ReportContextDto.ParameterValue.Builder getBuilder() {
            final ReportContextMessage.ReportContextDto.ParameterValue.Builder builder = getParameterValueDtoBuilder();
            if (value != null) {
                builder.setValue(value.getBuilder());
            }
            return builder;
        }

        @Override
        public String getName() {
            return parameterValueDto.getName();
        }

        @Override
        public IParameterValue<ParameterValue> setName(String name) {
            getParameterValueDtoBuilder().setName(name);
            return this;
        }

        @Override
        public IValue getValue() {
            if (value == null) {
                value = parameterValueDto != null ? Value.fromDto(parameterValueDto.getValue())
                        : (Value) MessagePool.getInstance().get(IValue.class);
            }
            return value;
        }

        @Override
        public IParameterValue<ParameterValue> setValue(IValue value) {
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
            parameterValueDto = null;
        }

        private ReportContextMessage.ReportContextDto.ParameterValue.Builder getParameterValueDtoBuilder() {
            if (parameterValueDto == null) {
                parameterValueDto = ReportContextMessage.ReportContextDto.ParameterValue.newBuilder();
            } else if (parameterValueDto instanceof ReportContextMessage.ReportContextDto.ParameterValue) {
                parameterValueDto = ((ReportContextMessage.ReportContextDto.ParameterValue) parameterValueDto).toBuilder();
            }
            return (ReportContextMessage.ReportContextDto.ParameterValue.Builder) parameterValueDto;
        }
    }

    public static class DimensionValue extends PoolableMessage<DimensionValue> implements IDimensionValue<DimensionValue> {
        private ReportContextMessage.ReportContextDto.DimensionOrBuilder dimensionValueDto;
        private Value value;

        DimensionValue() {
            super(IDimensionValue.class);
        }

        @Override
        public void setDto(Object dto) {
            dimensionValueDto = (ReportContextMessage.ReportContextDto.DimensionOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        ReportContextMessage.ReportContextDto.Dimension.Builder getBuilder() {
            final ReportContextMessage.ReportContextDto.Dimension.Builder builder = getDimensionValueDtoBuilder();
            if (value != null) {
                builder.setValue(value.getBuilder());
            }
            return builder;
        }

        @Override
        public String getName() {
            return dimensionValueDto.getName();
        }

        @Override
        public IDimensionValue<DimensionValue> setName(String name) {
            getDimensionValueDtoBuilder().setName(name);
            return this;
        }

        @Override
        public IValue getValue() {
            if (value == null) {
                value = dimensionValueDto != null ? Value.fromDto(dimensionValueDto.getValue())
                        : (Value) MessagePool.getInstance().get(IValue.class);
            }
            return value;
        }

        @Override
        public IDimensionValue<DimensionValue> setValue(IValue value) {
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
            dimensionValueDto = null;
        }

        private ReportContextMessage.ReportContextDto.Dimension.Builder getDimensionValueDtoBuilder() {
            if (dimensionValueDto == null) {
                dimensionValueDto = ReportContextMessage.ReportContextDto.Dimension.newBuilder();
            } else if (dimensionValueDto instanceof ReportContextMessage.ReportContextDto.Dimension) {
                dimensionValueDto = ((ReportContextMessage.ReportContextDto.Dimension) dimensionValueDto).toBuilder();
            }
            return (ReportContextMessage.ReportContextDto.Dimension.Builder) dimensionValueDto;
        }
    }

    public static class Value extends PoolableMessage<Value> implements IValue<Value> {
        private ReportContextMessage.ReportContextDto.ValueOrBuilder valueDto;
        private Object valueList;

        public static Value fromDto(ReportContextMessage.ReportContextDto.Value valueDto) {
            final Value value = (Value) MessagePool.getInstance().get(IValue.class);
            value.valueDto = valueDto;
            return value;
        }

        Value() {
            super(IValue.class);
        }

        @Override
        public ColumnType getType() {
            switch (getValueCase()) {
                case BOOLEANLIST: {
                    return ColumnType.Boolean;
                }
                case INTLIST: {
                    return ColumnType.Integer;
                }
                case LONGLIST: {
                    return ColumnType.Long;
                }
                case FLOATLIST: {
                    return ColumnType.Float;
                }
                case DOUBLELIST: {
                    return ColumnType.Double;
                }
                case STRINGLIST: {
                    return ColumnType.String;
                }
                case VALUE_NOT_SET: {
                    return null;
                }
                default: {
                    throw new UnsupportedOperationException("Unknown value type");
                }
            }
        }

        @Override
        public ValueLists.IBooleanList getBooleanValues() {
            final ReportContextMessage.ReportContextDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.BOOLEANLIST &&
                    valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Values are of type %s, cannot get booleans", valueCase));
            }
            if (valueList == null) {
                valueList = new ValueLists.IBooleanList() {
                    @Override
                    public int size() {
                        return valueDto.getBooleanList().getBooleanValueCount();
                    }

                    @Override
                    public boolean get(int index) {
                        return valueDto.getBooleanList().getBooleanValue(index);
                    }

                    @Override
                    public void add(boolean value) {
                        getValueDtoBuilder().getBooleanListBuilder().addBooleanValue(value);
                    }
                };
            }
            return (ValueLists.IBooleanList) valueList;
        }

        @Override
        public ValueLists.IIntegerList getIntegerValues() {
            final ReportContextMessage.ReportContextDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.INTLIST &&
                    valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Values are of type %s, cannot get integers", valueCase));
            }
            if (valueList == null) {
                valueList = new ValueLists.IIntegerList() {
                    @Override
                    public int size() {
                        return valueDto.getIntList().getIntValueCount();
                    }

                    @Override
                    public int get(int index) {
                        return valueDto.getIntList().getIntValue(index);
                    }

                    @Override
                    public void add(int value) {
                        getValueDtoBuilder().getIntListBuilder().addIntValue(value);
                    }
                };
            }
            return (ValueLists.IIntegerList) valueList;
        }

        @Override
        public ValueLists.ILongList getLongValues() {
            final ReportContextMessage.ReportContextDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.LONGLIST &&
                    valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Values are of type %s, cannot get longs", valueCase));
            }
            if (valueList == null) {
                valueList = new ValueLists.ILongList() {
                    @Override
                    public int size() {
                        return valueDto.getLongList().getLongValueCount();
                    }

                    @Override
                    public long get(int index) {
                        return valueDto.getLongList().getLongValue(index);
                    }

                    @Override
                    public void add(long value) {
                        getValueDtoBuilder().getLongListBuilder().addLongValue(value);
                    }
                };
            }
            return (ValueLists.ILongList) valueList;
        }

        @Override
        public ValueLists.IFloatList getFloatValues() {
            final ReportContextMessage.ReportContextDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.FLOATLIST &&
                    valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Values are of type %s, cannot get floats", valueCase));
            }
            if (valueList == null) {
                valueList = new ValueLists.IFloatList() {
                    @Override
                    public int size() {
                        return valueDto.getFloatList().getFloatValueCount();
                    }

                    @Override
                    public float get(int index) {
                        return valueDto.getFloatList().getFloatValue(index);
                    }

                    @Override
                    public void add(float value) {
                        getValueDtoBuilder().getFloatListBuilder().addFloatValue(value);
                    }
                };
            }
            return (ValueLists.IFloatList) valueList;
        }

        @Override
        public ValueLists.IDoubleList getDoubleValues() {
            final ReportContextMessage.ReportContextDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.DOUBLELIST &&
                    valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Values are of type %s, cannot get doubles", valueCase));
            }
            if (valueList == null) {
                valueList = new ValueLists.IDoubleList() {
                    @Override
                    public int size() {
                        return valueDto.getDoubleList().getDoubleValueCount();
                    }

                    @Override
                    public double get(int index) {
                        return valueDto.getDoubleList().getDoubleValue(index);
                    }

                    @Override
                    public void add(double value) {
                        getValueDtoBuilder().getDoubleListBuilder().addDoubleValue(value);
                    }
                };
            }
            return (ValueLists.IDoubleList) valueList;
        }

        @Override
        public ValueLists.IStringList getStringValues() {
            final ReportContextMessage.ReportContextDto.Value.ValueCase valueCase = getValueCase();
            if (valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.STRINGLIST &&
                    valueCase != ReportContextMessage.ReportContextDto.Value.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Values are of type %s, cannot get strings", valueCase));
            }
            if (valueList == null) {
                valueList = new ValueLists.IStringList() {
                    @Override
                    public int size() {
                        return valueDto.getStringList().getStringValueCount();
                    }

                    @Override
                    public String get(int index) {
                        return valueDto.getStringList().getStringValue(index);
                    }

                    @Override
                    public void add(String value) {
                        getValueDtoBuilder().getStringListBuilder().addStringValue(value);
                    }
                };
            }
            return (ValueLists.IStringList) valueList;
        }

        @Override
        protected void doRelease() {
            valueDto = null;
        }

        private ReportContextMessage.ReportContextDto.Value.ValueCase getValueCase() {
            // for some reason, this isn't in the ValueOrBuilder interface...
            if (valueDto == null) {
                return ReportContextMessage.ReportContextDto.Value.ValueCase.VALUE_NOT_SET;
            } else if (valueDto instanceof ReportContextMessage.ReportContextDto.Value) {
                return ((ReportContextMessage.ReportContextDto.Value) valueDto).getValueCase();
            } else {
                return ((ReportContextMessage.ReportContextDto.Value.Builder)valueDto).getValueCase();
            }
        }

        private ReportContextMessage.ReportContextDto.Value.Builder getValueDtoBuilder() {
            if (valueDto == null) {
                valueDto = ReportContextMessage.ReportContextDto.Value.newBuilder();
            } else if (valueDto instanceof ReportContextMessage.ReportContextDto.Value) {
                valueDto = ((ReportContextMessage.ReportContextDto.Value) valueDto).toBuilder();
            }
            return (ReportContextMessage.ReportContextDto.Value.Builder) valueDto;
        }


        ReportContextMessage.ReportContextDto.Value.Builder getBuilder() {
            return getValueDtoBuilder();
        }
    }
}
