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
import io.viewserver.messages.protobuf.dto.RowEventMessage;
import io.viewserver.messages.protobuf.dto.StatusMessage;
import io.viewserver.messages.protobuf.dto.TableEventMessage;
import io.viewserver.messages.tableevent.*;

import java.util.List;

/**
 * Created by nick on 02/12/15.
 */
public class TableEvent extends PoolableMessage<TableEvent> implements ITableEvent<TableEvent> {
    private TableEventMessage.TableEventDtoOrBuilder tableEventDto;
    private SchemaChange schemaChange;
    private RecyclingList<IRowEvent, RowEventMessage.RowEventDto> rowEventsList;
    private RecyclingList<IStatus, StatusMessage.StatusDto> statusesList;
    private TableMetadata tableMetadata;

    public static TableEvent fromDto(TableEventMessage.TableEventDto tableEventDto) {
        final TableEvent tableEvent = (TableEvent)MessagePool.getInstance().get(ITableEvent.class);
        tableEvent.tableEventDto = tableEventDto;
        return tableEvent;
    }

    TableEvent() {
        super(ITableEvent.class);
    }

    @Override
    public int getId() {
        return tableEventDto.getId();
    }

    @Override
    public ITableEvent setId(int id) {
        getTableEventBuilder().setId(id);
        return this;
    }

    @Override
    public int getInitSize() {
        return tableEventDto.getInitSize();
    }

    @Override
    public ITableEvent setInitSize(int initSize) {
        getTableEventBuilder().setInitSize(initSize);
        return this;
    }

    @Override
    public int getExecutionCycle() {
        return tableEventDto.getExecutionCycle();
    }

    @Override
    public ITableEvent setExecutionCycle(int executionCycle) {
        getTableEventBuilder().setExecutionCycle(executionCycle);
        return this;
    }

    @Override
    public boolean hasSchemaChange() {
        return schemaChange != null || (tableEventDto != null && tableEventDto.hasSchemaChange());
    }

    @Override
    public ISchemaChange getSchemaChange() {
        if (schemaChange == null) {
            schemaChange = tableEventDto != null ? SchemaChange.fromDto(tableEventDto.getSchemaChange())
                    : (SchemaChange) MessagePool.getInstance().get(ISchemaChange.class);
        }
        return schemaChange;
    }

    @Override
    public ITableEvent setSchemaChange(ISchemaChange schemaChange) {
        if (this.schemaChange != null) {
            this.schemaChange.release();
        }
        this.schemaChange = (SchemaChange) schemaChange.retain();
        return this;
    }

    @Override
    public List<IRowEvent> getRowEvents() {
        if (rowEventsList == null) {
            rowEventsList = new RecyclingList<IRowEvent, RowEventMessage.RowEventDto>(
                    IRowEvent.class) {
                @Override
                protected void doAdd(Object dto) {
                    final TableEventMessage.TableEventDto.Builder builder = getTableEventBuilder();
                    dtoList = builder.getRowEventsList();
                    if (dto instanceof RowEventMessage.RowEventDto) {
                        builder.addRowEvents((RowEventMessage.RowEventDto) dto);
                    } else {
                        builder.addRowEvents((RowEventMessage.RowEventDto.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getTableEventBuilder().clearRowEvents();
                }
            };
        }
        rowEventsList.setDtoList(tableEventDto != null ? tableEventDto.getRowEventsList() : null);
        return rowEventsList;
    }

    @Override
    public List<IStatus> getStatuses() {
        if (statusesList == null) {
            statusesList = new RecyclingList<IStatus, StatusMessage.StatusDto>(
                    IStatus.class) {
                @Override
                protected void doAdd(Object dto) {
                    final TableEventMessage.TableEventDto.Builder builder = getTableEventBuilder();
                    dtoList = builder.getStatusesList();
                    if (dto instanceof StatusMessage.StatusDto) {
                        builder.addStatuses((StatusMessage.StatusDto) dto);
                    } else {
                        builder.addStatuses((StatusMessage.StatusDto.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getTableEventBuilder().clearStatuses();
                }
            };
        }
        statusesList.setDtoList(tableEventDto != null ? tableEventDto.getStatusesList() : null);
        return statusesList;
    }

    @Override
    public int getFlags() {
        return tableEventDto.getFlags();
    }

    @Override
    public ITableEvent setFlags(int flags) {
        getTableEventBuilder().setFlags(flags);
        return this;
    }

    @Override
    public ITableMetadata getMetadata() {
        if (tableMetadata == null) {
            tableMetadata = tableEventDto != null ? TableMetadata.fromDto(tableEventDto.getMetaData())
                    : (TableMetadata) MessagePool.getInstance().get(ITableMetadata.class);
        }
        return tableMetadata;
    }

    @Override
    public ITableEvent setMetadata(ITableMetadata metadata) {
        if (tableMetadata != null) {
            tableMetadata.release();
        }
        tableMetadata = (TableMetadata)metadata.retain();
        return this;
    }

    @Override
    protected void doRelease() {
        if (schemaChange != null) {
            schemaChange.release();
            schemaChange = null;
        }
        if (rowEventsList != null) {
            rowEventsList.release();
        }
        if (statusesList != null) {
            statusesList.release();
        }
        if (tableMetadata != null) {
            tableMetadata.release();
            tableMetadata = null;
        }
        tableEventDto = null;
    }

    private TableEventMessage.TableEventDto.Builder getTableEventBuilder() {
        if (tableEventDto == null) {
            tableEventDto = TableEventMessage.TableEventDto.newBuilder();
        } else if (tableEventDto instanceof TableEventMessage.TableEventDto) {
            tableEventDto = ((TableEventMessage.TableEventDto) tableEventDto).toBuilder();
        }
        return (TableEventMessage.TableEventDto.Builder) tableEventDto;
    }

    TableEventMessage.TableEventDto.Builder getBuilder() {
        final TableEventMessage.TableEventDto.Builder builder = getTableEventBuilder();
        if (schemaChange != null) {
            builder.setSchemaChange(schemaChange.getBuilder());
        }
        if (tableMetadata != null) {
            builder.setMetaData(tableMetadata.getBuilder());
        }
        return builder;
    }
}
