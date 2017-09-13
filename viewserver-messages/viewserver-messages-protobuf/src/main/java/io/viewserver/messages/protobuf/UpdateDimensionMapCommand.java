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
import io.viewserver.messages.command.IUpdateDimensionMapCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.UpdateDimensionMapCommandMessage;

import java.util.List;

/**
 * Created by nick on 07/12/15.
 */
public class UpdateDimensionMapCommand extends PoolableMessage<UpdateDimensionMapCommand>
    implements IUpdateDimensionMapCommand<UpdateDimensionMapCommand>, ICommandExtension<UpdateDimensionMapCommand> {
    private UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDtoOrBuilder updateDimensionMapCommandDto;
    private RecyclingList<IDataSource, UpdateDimensionMapCommandMessage.DataSource> dataSources;

    public static UpdateDimensionMapCommand fromDto(UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto dto) {
        final UpdateDimensionMapCommand updateDimensionMapCommand = (UpdateDimensionMapCommand) MessagePool.getInstance().get(IUpdateDimensionMapCommand.class);
        updateDimensionMapCommand.updateDimensionMapCommandDto = dto;
        return updateDimensionMapCommand;
    }

    UpdateDimensionMapCommand() {
        super(IUpdateDimensionMapCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        updateDimensionMapCommandDto = (UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(UpdateDimensionMapCommandMessage.updateDimensionMapCommand, getBuilder().buildPartial());
    }

    @Override
    public List<IDataSource> getDataSources() {
        if (dataSources == null) {
            dataSources = new RecyclingList<IDataSource, UpdateDimensionMapCommandMessage.DataSource>(
                    IDataSource.class
            ) {
                @Override
                protected void doAdd(Object dto) {
                    final UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto.Builder builder = getUpdateDimensionMapCommandDtoBuilder();
                    dtoList = builder.getDataSourceList();
                    if (dto instanceof UpdateDimensionMapCommandMessage.DataSource) {
                        builder.addDataSource((UpdateDimensionMapCommandMessage.DataSource) dto);
                    } else {
                        builder.addDataSource((UpdateDimensionMapCommandMessage.DataSource.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getUpdateDimensionMapCommandDtoBuilder().clearDataSource();
                }
            };
        }
        dataSources.setDtoList(updateDimensionMapCommandDto != null ? updateDimensionMapCommandDto.getDataSourceList() : null);
        return dataSources;
    }

    @Override
    protected void doRelease() {
        if (dataSources != null) {
            dataSources.release();
        }
        updateDimensionMapCommandDto = null;
    }

    UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto.Builder getBuilder() {
        return getUpdateDimensionMapCommandDtoBuilder();
    }

    private UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto.Builder getUpdateDimensionMapCommandDtoBuilder() {
        if (updateDimensionMapCommandDto == null) {
            updateDimensionMapCommandDto = UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto.newBuilder();
        } else if (updateDimensionMapCommandDto instanceof UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto) {
            updateDimensionMapCommandDto = ((UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto) updateDimensionMapCommandDto).toBuilder();
        }
        return (UpdateDimensionMapCommandMessage.UpdateDimensionMapCommandDto.Builder) updateDimensionMapCommandDto;
    }

    public static class DataSource extends PoolableMessage<DataSource> implements IDataSource<DataSource> {
        private UpdateDimensionMapCommandMessage.DataSourceOrBuilder dataSourceDto;
        private RecyclingList<IDimension, UpdateDimensionMapCommandMessage.Dimension> dimensions;

        DataSource() {
            super(IDataSource.class);
        }

        @Override
        public void setDto(Object dto) {
            dataSourceDto = (UpdateDimensionMapCommandMessage.DataSourceOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return dataSourceDto.getName();
        }

        @Override
        public IDataSource<DataSource> setName(String name) {
            getDataSourceDtoBuilder().setName(name);
            return this;
        }

        @Override
        public List<IDimension> getDimensions() {
            if (dimensions == null) {
                dimensions = new RecyclingList<IDimension, UpdateDimensionMapCommandMessage.Dimension>(
                        IDimension.class
                ) {
                    @Override
                    protected void doAdd(Object dto) {
                        final UpdateDimensionMapCommandMessage.DataSource.Builder builder = getDataSourceDtoBuilder();
                        dtoList = builder.getDimensionList();
                        if (dto instanceof UpdateDimensionMapCommandMessage.Dimension) {
                            builder.addDimension((UpdateDimensionMapCommandMessage.Dimension) dto);
                        } else {
                            builder.addDimension((UpdateDimensionMapCommandMessage.Dimension.Builder) dto);
                        }
                    }

                    @Override
                    protected void doClear() {
                        getDataSourceDtoBuilder().clearDimension();
                    }
                };
            }
            dimensions.setDtoList(dataSourceDto != null ? dataSourceDto.getDimensionList() : null);
            return dimensions;
        }

        @Override
        protected void doRelease() {
            if (dimensions != null) {
                dimensions.release();
            }
            dataSourceDto = null;
        }

        UpdateDimensionMapCommandMessage.DataSource.Builder getBuilder() {
            return getDataSourceDtoBuilder();
        }

        private UpdateDimensionMapCommandMessage.DataSource.Builder getDataSourceDtoBuilder() {
            if (dataSourceDto == null) {
                dataSourceDto = UpdateDimensionMapCommandMessage.DataSource.newBuilder();
            } else if (dataSourceDto instanceof UpdateDimensionMapCommandMessage.DataSource) {
                dataSourceDto = ((UpdateDimensionMapCommandMessage.DataSource) dataSourceDto).toBuilder();
            }
            return (UpdateDimensionMapCommandMessage.DataSource.Builder) dataSourceDto;
        }
    }

    public static class Dimension extends PoolableMessage<Dimension> implements IDimension<Dimension> {
        private UpdateDimensionMapCommandMessage.DimensionOrBuilder dimensionDto;
        private RecyclingList<IMapping, UpdateDimensionMapCommandMessage.Mapping> mappings;

        Dimension() {
            super(IDimension.class);
        }

        @Override
        public void setDto(Object dto) {
            dimensionDto = (UpdateDimensionMapCommandMessage.DimensionOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return dimensionDto.getName();
        }

        @Override
        public IDimension setName(String name) {
            getDimensionDtoBuilder().setName(name);
            return this;
        }

        @Override
        public List<IMapping> getMappings() {
            if (mappings == null) {
                mappings = new RecyclingList<IMapping, UpdateDimensionMapCommandMessage.Mapping>(
                        IMapping.class
                ) {
                    @Override
                    protected void doAdd(Object dto) {
                        final UpdateDimensionMapCommandMessage.Dimension.Builder builder = getDimensionDtoBuilder();
                        dtoList = builder.getMappingList();
                        if (dto instanceof UpdateDimensionMapCommandMessage.Dimension) {
                            builder.addMapping((UpdateDimensionMapCommandMessage.Mapping) dto);
                        } else {
                            builder.addMapping((UpdateDimensionMapCommandMessage.Mapping.Builder) dto);
                        }
                    }

                    @Override
                    protected void doClear() {
                        getDimensionDtoBuilder().clearMapping();
                    }
                };
            }
            mappings.setDtoList(dimensionDto != null ? dimensionDto.getMappingList() : null);
            return mappings;
        }

        @Override
        protected void doRelease() {
            if (mappings != null) {
                mappings.release();
            }
            dimensionDto = null;
        }

        UpdateDimensionMapCommandMessage.Dimension.Builder getBuilder() {
            return getDimensionDtoBuilder();
        }

        private UpdateDimensionMapCommandMessage.Dimension.Builder getDimensionDtoBuilder() {
            if (dimensionDto == null) {
                dimensionDto = UpdateDimensionMapCommandMessage.Dimension.newBuilder();
            } else if (dimensionDto instanceof UpdateDimensionMapCommandMessage.Dimension) {
                dimensionDto = ((UpdateDimensionMapCommandMessage.Dimension) dimensionDto).toBuilder();
            }
            return (UpdateDimensionMapCommandMessage.Dimension.Builder) dimensionDto;
        }
    }

    public static class Mapping extends PoolableMessage<Mapping> implements IMapping<Mapping> {
        private UpdateDimensionMapCommandMessage.MappingOrBuilder mappingDto;

        Mapping() {
            super(IMapping.class);
        }

        @Override
        public void setDto(Object dto) {
            mappingDto = (UpdateDimensionMapCommandMessage.MappingOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public int getId() {
            return mappingDto.getId();
        }

        @Override
        public IMapping<Mapping> setId(int id) {
            getMappingDtoBuilder().setId(id);
            return this;
        }

        @Override
        public boolean getBooleanValue() {
            final UpdateDimensionMapCommandMessage.Mapping.ValueCase valueCase = getValueCase();
            if (valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.BOOLEANVALUE &&
                    valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value is of type %s, cannot get boolean", valueCase));
            }
            return mappingDto.getBooleanValue();
        }

        @Override
        public IMapping<Mapping> setBooleanValue(boolean value) {
            getMappingDtoBuilder().setBooleanValue(value);
            return this;
        }

        @Override
        public int getIntegerValue() {
            final UpdateDimensionMapCommandMessage.Mapping.ValueCase valueCase = getValueCase();
            if (valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.INTVALUE &&
                    valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value is of type %s, cannot get integer", valueCase));
            }
            return mappingDto.getIntValue();
        }

        @Override
        public IMapping<Mapping> setIntegerValue(int value) {
            getMappingDtoBuilder().setIntValue(value);
            return this;
        }

        @Override
        public long getLongValue() {
            final UpdateDimensionMapCommandMessage.Mapping.ValueCase valueCase = getValueCase();
            if (valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.LONGVALUE &&
                    valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value is of type %s, cannot get long", valueCase));
            }
            return mappingDto.getLongValue();
        }

        @Override
        public IMapping<Mapping> setLongValue(long value) {
            getMappingDtoBuilder().setLongValue(value);
            return this;
        }

        @Override
        public String getStringValue() {
            final UpdateDimensionMapCommandMessage.Mapping.ValueCase valueCase = getValueCase();
            if (valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.STRINGVALUE &&
                    valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value is of type %s, cannot get string", valueCase));
            }
            return mappingDto.getStringValue();
        }

        @Override
        public IMapping<Mapping> setStringValue(String value) {
            getMappingDtoBuilder().setStringValue(value);
            return this;
        }

        @Override
        public boolean getNullValue() {
            final UpdateDimensionMapCommandMessage.Mapping.ValueCase valueCase = getValueCase();
            if (valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.NULLVALUE &&
                    valueCase != UpdateDimensionMapCommandMessage.Mapping.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value is of type %s, cannot get null", valueCase));
            }
            return true;
        }

        @Override
        public IMapping<Mapping> setNullValue() {
            getMappingDtoBuilder().setNullValue(1);
            return this;
        }

        @Override
        protected void doRelease() {
            mappingDto = null;
        }

        UpdateDimensionMapCommandMessage.Mapping.Builder getBuilder() {
            return getMappingDtoBuilder();
        }

        private UpdateDimensionMapCommandMessage.Mapping.Builder getMappingDtoBuilder() {
            if (mappingDto == null) {
                mappingDto = UpdateDimensionMapCommandMessage.Mapping.newBuilder();
            } else if (mappingDto instanceof UpdateDimensionMapCommandMessage.Mapping) {
                mappingDto = ((UpdateDimensionMapCommandMessage.Mapping) mappingDto).toBuilder();
            }
            return (UpdateDimensionMapCommandMessage.Mapping.Builder) mappingDto;
        }

        private UpdateDimensionMapCommandMessage.Mapping.ValueCase getValueCase() {
            // for some reason, this isn't in the ValueOrBuilder interface...
            if (mappingDto == null) {
                return UpdateDimensionMapCommandMessage.Mapping.ValueCase.VALUE_NOT_SET;
            } else if (mappingDto instanceof UpdateDimensionMapCommandMessage.Mapping) {
                return ((UpdateDimensionMapCommandMessage.Mapping) mappingDto).getValueCase();
            } else {
                return ((UpdateDimensionMapCommandMessage.Mapping.Builder)mappingDto).getValueCase();
            }
        }
    }
}
