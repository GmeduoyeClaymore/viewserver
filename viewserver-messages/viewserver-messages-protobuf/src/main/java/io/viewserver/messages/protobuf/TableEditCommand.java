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
import io.viewserver.messages.command.ITableEditCommand;
import io.viewserver.messages.protobuf.dto.CommandMessage;
import io.viewserver.messages.protobuf.dto.TableEditCommandMessage;
import io.viewserver.messages.tableevent.ITableEvent;

/**
 * Created by nick on 07/12/15.
 */
public class TableEditCommand extends PoolableMessage<TableEditCommand> implements ITableEditCommand<TableEditCommand>,
        ICommandExtension<TableEditCommand> {
    private TableEditCommandMessage.TableEditCommandDtoOrBuilder tableEditCommandDto;
    private TableEvent tableEvent;
    private CreationConfig creationConfig;

    TableEditCommand() {
        super(ITableEditCommand.class);
    }

    @Override
    public void setDto(Object dto) {
        tableEditCommandDto = (TableEditCommandMessage.TableEditCommandDtoOrBuilder) dto;
    }

    @Override
    public void build(CommandMessage.CommandDto.Builder commandDtoBuilder) {
        commandDtoBuilder.setExtension(TableEditCommandMessage.tableEditCommand, getBuilder().buildPartial());
    }

    @Override
    public String getTableName() {
        return tableEditCommandDto.getTableName();
    }

    @Override
    public ITableEditCommand<TableEditCommand> setTableName(String tableName) {
        getTableEditCommandDtoBuilder().setTableName(tableName);
        return this;
    }

    @Override
    public ITableEvent getTableEvent() {
        if (tableEvent == null) {
            tableEvent = tableEditCommandDto != null ? TableEvent.fromDto(tableEditCommandDto.getTableEvent())
                    : (TableEvent) MessagePool.getInstance().get(ITableEvent.class);
        }
        return tableEvent;
    }

    @Override
    public ITableEditCommand<TableEditCommand> setTableEvent(ITableEvent tableEvent) {
        if (this.tableEvent != null) {
            this.tableEvent.release();
        }
        this.tableEvent = (TableEvent)tableEvent.retain();
        return this;
    }

    @Override
    public Operation getOperation() {
        final TableEditCommandMessage.TableEditCommandDto.Operation operation = tableEditCommandDto.getOperation();
        switch (operation) {
            case CREATE: {
                return Operation.Create;
            }
            case EDIT: {
                return Operation.Edit;
            }
            case DELETE: {
                return Operation.Delete;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown operatorion '%s'", operation));
            }
        }
    }

    @Override
    public ITableEditCommand<TableEditCommand> setOperation(Operation operation) {
        switch (operation) {
            case Create: {
                getTableEditCommandDtoBuilder().setOperation(TableEditCommandMessage.TableEditCommandDto.Operation.CREATE);
                break;
            }
            case Edit: {
                getTableEditCommandDtoBuilder().setOperation(TableEditCommandMessage.TableEditCommandDto.Operation.EDIT);
                break;
            }
            case Delete: {
                getTableEditCommandDtoBuilder().setOperation(TableEditCommandMessage.TableEditCommandDto.Operation.DELETE);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown operation '%s'", operation));
            }
        }
        return this;
    }

    @Override
    public ICreationConfig getCreationConfig() {
        if (creationConfig == null) {
            creationConfig = tableEditCommandDto != null ? CreationConfig.fromDto(tableEditCommandDto.getCreationConfig())
                    : (CreationConfig)MessagePool.getInstance().get(ICreationConfig.class);
        }
        return creationConfig;
    }

    @Override
    public ITableEditCommand setCreationConfig(ICreationConfig creationConfig) {
        if (this.creationConfig != null) {
            this.creationConfig.release();
        }
        if(creationConfig == null){
            return this;
        }
        this.creationConfig = (CreationConfig) creationConfig.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        if (tableEvent != null) {
            tableEvent.release();
            tableEvent = null;
        }
        if (creationConfig != null) {
            creationConfig.release();
            creationConfig = null;
        }
        tableEditCommandDto = null;
    }

    TableEditCommandMessage.TableEditCommandDto.Builder getBuilder() {
        final TableEditCommandMessage.TableEditCommandDto.Builder builder = getTableEditCommandDtoBuilder();
        if (tableEvent != null) {
            builder.setTableEvent(tableEvent.getBuilder());
        }
        if (creationConfig != null) {
            builder.setCreationConfig(creationConfig.getBuilder());
        }
        return builder;
    }

    private TableEditCommandMessage.TableEditCommandDto.Builder getTableEditCommandDtoBuilder() {
        if (tableEditCommandDto == null) {
            tableEditCommandDto = TableEditCommandMessage.TableEditCommandDto.newBuilder();
        } else if (tableEditCommandDto instanceof TableEditCommandMessage.TableEditCommandDto) {
            tableEditCommandDto = ((TableEditCommandMessage.TableEditCommandDto) tableEditCommandDto).toBuilder();
        }
        return (TableEditCommandMessage.TableEditCommandDto.Builder) tableEditCommandDto;
    }

    public static class CreationConfig extends PoolableMessage<CreationConfig> implements ICreationConfig<CreationConfig> {
        private TableEditCommandMessage.TableEditCommandDto.CreationConfigOrBuilder creationConfigDto;

        public static CreationConfig fromDto(TableEditCommandMessage.TableEditCommandDto.CreationConfig dto) {
            final CreationConfig creationConfig = (CreationConfig) MessagePool.getInstance().get(ICreationConfig.class);
            creationConfig.creationConfigDto = dto;
            return creationConfig;
        }

        CreationConfig() {
            super(ICreationConfig.class);
        }

        @Override
        public String getTableType() {
            return creationConfigDto.getTableType();
        }

        @Override
        public ICreationConfig setTableType(String tableType) {
            getCreationConfigDtoBuilder().setTableType(tableType);
            return this;
        }

        @Override
        public Object getConfig() {
            return getCreationConfigDtoBuilder().getConfig();
        }

        @Override
        public ICreationConfig setConfig(Object config) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doRelease() {
            creationConfigDto = null;
        }

        TableEditCommandMessage.TableEditCommandDto.CreationConfig.Builder getBuilder() {
            return getCreationConfigDtoBuilder();
        }

        private TableEditCommandMessage.TableEditCommandDto.CreationConfig.Builder getCreationConfigDtoBuilder() {
            if (creationConfigDto == null) {
                creationConfigDto = TableEditCommandMessage.TableEditCommandDto.CreationConfig.newBuilder();
            } else if (creationConfigDto instanceof TableEditCommandMessage.TableEditCommandDto.CreationConfig) {
                creationConfigDto = ((TableEditCommandMessage.TableEditCommandDto.CreationConfig) creationConfigDto).toBuilder();
            }
            return (TableEditCommandMessage.TableEditCommandDto.CreationConfig.Builder) creationConfigDto;
        }
    }
}
