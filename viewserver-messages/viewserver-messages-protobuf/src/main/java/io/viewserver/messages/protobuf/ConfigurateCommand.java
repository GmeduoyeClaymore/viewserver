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
import io.viewserver.messages.command.IConfigurateCommand;
import io.viewserver.messages.common.ColumnType;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.ConfiguratorMessage;
import io.viewserver.messages.protobuf.dto.OperatorMessage;

import java.util.List;

/**
 * Created by bemm on 03/12/15.
 */
public class ConfigurateCommand extends PoolableMessage<ConfigurateCommand> implements IConfigurateCommand<ConfigurateCommand>,
        ICommandExtension<ConfigurateCommand> {
    private ConfiguratorMessage.ConfiguratorDtoOrBuilder configuratorDto;
    private RecyclingList<IOperator, OperatorMessage.OperatorDto> operators;

    ConfigurateCommand() {
        super(IConfigurateCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        configuratorDto = (ConfiguratorMessage.ConfiguratorDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(ConfiguratorMessage.configurator, getBuilder().buildPartial());
    }

    @Override
    public List<IOperator> getOperators() {
        if (operators == null) {
            operators = new RecyclingList<IOperator, OperatorMessage.OperatorDto>(
                    IOperator.class
            ) {
                @Override
                protected void doAdd(Object dto) {
                    final ConfiguratorMessage.ConfiguratorDto.Builder builder = getConfiguratorDtoBuilder();
                    dtoList = builder.getOperatorsList();
                    if (dto instanceof OperatorMessage.OperatorDto) {
                        builder.addOperators((OperatorMessage.OperatorDto) dto);
                    } else {
                        builder.addOperators((OperatorMessage.OperatorDto.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getConfiguratorDtoBuilder().clearOperators();
                }
            };
        }
        operators.setDtoList(configuratorDto != null ? configuratorDto.getOperatorsList() : null);
        return operators;
    }

    @Override
    protected void doRelease() {
        if (operators != null) {
            operators.release();
        }
        configuratorDto = null;
    }

    ConfiguratorMessage.ConfiguratorDto.Builder getBuilder() {
        return getConfiguratorDtoBuilder();
    }

    private ConfiguratorMessage.ConfiguratorDto.Builder getConfiguratorDtoBuilder() {
        if (configuratorDto == null) {
            configuratorDto = ConfiguratorMessage.ConfiguratorDto.newBuilder();
        } else if (configuratorDto instanceof ConfiguratorMessage.ConfiguratorDto) {
            configuratorDto = ((ConfiguratorMessage.ConfiguratorDto) configuratorDto).toBuilder();
        }
        return (ConfiguratorMessage.ConfiguratorDto.Builder) configuratorDto;
    }

    public static class Operator extends PoolableMessage<Operator> implements IOperator<Operator> {
        private OperatorMessage.OperatorDtoOrBuilder operatorDto;
        private RecyclingList<IConnection, OperatorMessage.ConnectionDto> connections;
        private RecyclingList<IMetadataItem, OperatorMessage.MetadataItem> metadataItems;
        private IOperatorConfig<?> config;

        Operator() {
            super(IOperator.class);
        }

        @Override
        public void setDto(Object dto) {
            operatorDto = (OperatorMessage.OperatorDtoOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return operatorDto.getName();
        }

        @Override
        public IOperator<Operator> setName(String name) {
            getOperatorDtoBuilder().setName(name);
            return this;
        }

        @Override
        public String getType() {
            return operatorDto.getType();
        }

        @Override
        public IOperator<Operator> setType(String type) {
            getOperatorDtoBuilder().setType(type);
            return this;
        }

        @Override
        public Operation getOperation() {
            final OperatorMessage.OperatorDto.Operation operation = operatorDto.getOperation();
            switch (operation) {
                case CREATECONFIGURE: {
                    return Operation.CreateConfigure;
                }
                case REMOVE: {
                    return Operation.Remove;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("Unknown operation '%s'", operation));
                }
            }
        }

        @Override
        public IOperator<Operator> setOperation(Operation operation) {
            switch (operation) {
                case CreateConfigure: {
                    getOperatorDtoBuilder().setOperation(OperatorMessage.OperatorDto.Operation.CREATECONFIGURE);
                    break;
                }
                case Remove: {
                    getOperatorDtoBuilder().setOperation(OperatorMessage.OperatorDto.Operation.REMOVE);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Unknown operation '%s'", operation));
                }
            }
            return this;
        }

        @Override
        public boolean hasConfig() {
            return config != null || (operatorDto != null && operatorDto.hasConfig());
        }

        @Override
        public <T extends IOperatorConfig> T getConfig(Class<T> configClass) {
            if (config == null) {
                config = operatorDto.hasConfig()
                        ? OperatorConfigCoder.INSTANCE.decode(operatorDto.getConfig(), configClass)
                        : MessagePool.getInstance().get(configClass);
            }
            return (T) config;
        }

        @Override
        public IOperator<Operator> setConfig(IOperatorConfig<?> config) {
            if (this.config != null) {
                this.config.release();
            }
            this.config = (IOperatorConfig<?>) config.retain();
            return this;
        }

        @Override
        public List<IConnection> getConnections() {
            if (connections == null) {
                connections = new RecyclingList<IConnection, OperatorMessage.ConnectionDto>(
                        IConnection.class
                ) {
                    @Override
                    protected void doAdd(Object dto) {
                        final OperatorMessage.OperatorDto.Builder builder = getOperatorDtoBuilder();
                        dtoList = builder.getConnectionsList();
                        if (dto instanceof OperatorMessage.ConnectionDto) {
                            builder.addConnections((OperatorMessage.ConnectionDto) dto);
                        } else {
                            builder.addConnections((OperatorMessage.ConnectionDto.Builder) dto);
                        }
                    }

                    @Override
                    protected void doClear() {
                        getOperatorDtoBuilder().clearConnections();
                    }
                };
            }
            connections.setDtoList(operatorDto != null ? operatorDto.getConnectionsList() : null);
            return connections;
        }

        @Override
        public List<IMetadataItem> getMetadataItems() {
            if (metadataItems == null) {
                metadataItems = new RecyclingList<IMetadataItem, OperatorMessage.MetadataItem>(
                        IMetadataItem.class
                ) {
                    @Override
                    protected void doAdd(Object dto) {
                        final OperatorMessage.OperatorDto.Builder builder = getOperatorDtoBuilder();
                        dtoList = builder.getMetadataList();
                        if (dto instanceof OperatorMessage.MetadataItem) {
                            builder.addMetadata((OperatorMessage.MetadataItem) dto);
                        } else {
                            builder.addMetadata((OperatorMessage.MetadataItem.Builder) dto);
                        }
                    }

                    @Override
                    protected void doClear() {
                        getOperatorDtoBuilder().clearMetadata();
                    }
                };
            }
            metadataItems.setDtoList(operatorDto != null ? operatorDto.getMetadataList() : null);
            return metadataItems;
        }

        @Override
        protected void doRelease() {
            if (connections != null) {
                connections.release();
            }
            if (metadataItems != null) {
                metadataItems.release();
            }
            if (config != null) {
                config.release();
                config = null;
            }
            operatorDto = null;
        }

        OperatorMessage.OperatorDto.Builder getBuilder() {
            final OperatorMessage.OperatorDto.Builder builder = getOperatorDtoBuilder();
            if (config != null) {
                builder.setConfig(OperatorConfigCoder.INSTANCE.encode(config));
            }
            return builder;
        }

        private OperatorMessage.OperatorDto.Builder getOperatorDtoBuilder() {
            if (operatorDto == null) {
                operatorDto = OperatorMessage.OperatorDto.newBuilder();
            } else if (operatorDto instanceof OperatorMessage.OperatorDto) {
                operatorDto = ((OperatorMessage.OperatorDto) operatorDto).toBuilder();
            }
            return (OperatorMessage.OperatorDto.Builder) operatorDto;
        }
    }

    public static class Connection extends PoolableMessage<Connection> implements IConnection<Connection> {
        private OperatorMessage.ConnectionDtoOrBuilder connectionDto;

        Connection() {
            super(IConnection.class);
        }

        @Override
        public void setDto(Object dto) {
            connectionDto = (OperatorMessage.ConnectionDtoOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getInput() {
            return connectionDto.getInput();
        }

        @Override
        public IConnection<Connection> setInput(String input) {
            getConnectionDtoBuilder().setInput(input);
            return this;
        }

        @Override
        public String getOperator() {
            return connectionDto.getOperator();
        }

        @Override
        public IConnection<Connection> setOperator(String operator) {
            getConnectionDtoBuilder().setOperator(operator);
            return this;
        }

        @Override
        public String getOutput() {
            return connectionDto.getOutput();
        }

        @Override
        public IConnection<Connection> setOutput(String output) {
            getConnectionDtoBuilder().setOutput(output);
            return this;
        }

        @Override
        protected void doRelease() {
            connectionDto = null;
        }

        OperatorMessage.ConnectionDto.Builder getBuilder() {
            return getConnectionDtoBuilder();
        }

        private OperatorMessage.ConnectionDto.Builder getConnectionDtoBuilder() {
            if (connectionDto == null) {
                connectionDto = OperatorMessage.ConnectionDto.newBuilder();
            } else if (connectionDto instanceof OperatorMessage.ConnectionDto) {
                connectionDto = ((OperatorMessage.ConnectionDto) connectionDto).toBuilder();
            }
            return (OperatorMessage.ConnectionDto.Builder) connectionDto;
        }
    }

    public static class MetadataItem extends PoolableMessage<OperatorMessage.MetadataItem> implements IMetadataItem<OperatorMessage.MetadataItem> {
        private OperatorMessage.MetadataItemOrBuilder metadataItemDto;

        MetadataItem() {
            super(IMetadataItem.class);
        }

        @Override
        public void setDto(Object dto) {
            metadataItemDto = (OperatorMessage.MetadataItemOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getKey() {
            return metadataItemDto.getKey();
        }

        @Override
        public IMetadataItem<OperatorMessage.MetadataItem> setKey(String key) {
            getMetadataItemDtoBuilder().setKey(key);
            return this;
        }

        @Override
        public ColumnType getType() {
            final OperatorMessage.MetadataItem.ValueCase valueCase = getValueCase();
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
            final OperatorMessage.MetadataItem.ValueCase valueCase = getValueCase();
            if (valueCase != OperatorMessage.MetadataItem.ValueCase.BOOLEANVALUE &&
                    valueCase != OperatorMessage.MetadataItem.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get boolean value", valueCase));
            }
            return metadataItemDto.getBooleanValue();
        }

        @Override
        public IMetadataItem<OperatorMessage.MetadataItem> setBooleanValue(boolean value) {
            getMetadataItemDtoBuilder().setBooleanValue(value);
            return this;
        }

        @Override
        public int getIntegerValue() {
            final OperatorMessage.MetadataItem.ValueCase valueCase = getValueCase();
            if (valueCase != OperatorMessage.MetadataItem.ValueCase.INTVALUE &&
                    valueCase != OperatorMessage.MetadataItem.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get integer value", valueCase));
            }
            return metadataItemDto.getIntValue();
        }

        @Override
        public IMetadataItem<OperatorMessage.MetadataItem> setIntegerValue(int value) {
            getMetadataItemDtoBuilder().setIntValue(value);
            return this;
        }

        @Override
        public long getLongValue() {
            final OperatorMessage.MetadataItem.ValueCase valueCase = getValueCase();
            if (valueCase != OperatorMessage.MetadataItem.ValueCase.LONGVALUE &&
                    valueCase != OperatorMessage.MetadataItem.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get long value", valueCase));
            }
            return metadataItemDto.getLongValue();
        }

        @Override
        public IMetadataItem<OperatorMessage.MetadataItem> setLongValue(long value) {
            getMetadataItemDtoBuilder().setLongValue(value);
            return this;
        }

        @Override
        public float getFloatValue() {
            final OperatorMessage.MetadataItem.ValueCase valueCase = getValueCase();
            if (valueCase != OperatorMessage.MetadataItem.ValueCase.FLOATVALUE &&
                    valueCase != OperatorMessage.MetadataItem.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get float value", valueCase));
            }
            return metadataItemDto.getFloatValue();
        }

        @Override
        public IMetadataItem<OperatorMessage.MetadataItem> setFloatValue(float value) {
            getMetadataItemDtoBuilder().setFloatValue(value);
            return this;
        }

        @Override
        public double getDoubleValue() {
            final OperatorMessage.MetadataItem.ValueCase valueCase = getValueCase();
            if (valueCase != OperatorMessage.MetadataItem.ValueCase.DOUBLEVALUE &&
                    valueCase != OperatorMessage.MetadataItem.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get double value", valueCase));
            }
            return metadataItemDto.getDoubleValue();
        }

        @Override
        public IMetadataItem<OperatorMessage.MetadataItem> setDoubleValue(double value) {
            getMetadataItemDtoBuilder().setDoubleValue(value);
            return this;
        }

        @Override
        public String getStringValue() {
            final OperatorMessage.MetadataItem.ValueCase valueCase = getValueCase();
            if (valueCase != OperatorMessage.MetadataItem.ValueCase.STRINGVALUE &&
                    valueCase != OperatorMessage.MetadataItem.ValueCase.VALUE_NOT_SET) {
                throw new UnsupportedOperationException(String.format("Value are of type %s, cannot get string value", valueCase));
            }
            return metadataItemDto.getStringValue();
        }

        @Override
        public IMetadataItem<OperatorMessage.MetadataItem> setStringValue(String value) {
            getMetadataItemDtoBuilder().setStringValue(value);
            return this;
        }

        @Override
        protected void doRelease() {
            metadataItemDto = null;
        }

        OperatorMessage.MetadataItem.Builder getBuilder() {
            return getMetadataItemDtoBuilder();
        }

        private OperatorMessage.MetadataItem.Builder getMetadataItemDtoBuilder() {
            if (metadataItemDto == null) {
                metadataItemDto = OperatorMessage.MetadataItem.newBuilder();
            } else if (metadataItemDto instanceof OperatorMessage.MetadataItem) {
                metadataItemDto = ((OperatorMessage.MetadataItem) metadataItemDto).toBuilder();
            }
            return (OperatorMessage.MetadataItem.Builder) metadataItemDto;
        }

        private OperatorMessage.MetadataItem.ValueCase getValueCase() {
            // for some reason, this isn't in the ValueOrBuilder interface...
            if (metadataItemDto == null) {
                return OperatorMessage.MetadataItem.ValueCase.VALUE_NOT_SET;
            } else if (metadataItemDto instanceof OperatorMessage.MetadataItem) {
                return ((OperatorMessage.MetadataItem) metadataItemDto).getValueCase();
            } else {
                return ((OperatorMessage.MetadataItem.Builder) metadataItemDto).getValueCase();
            }
        }
    }
}
