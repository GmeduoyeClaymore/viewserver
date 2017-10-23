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

package io.viewserver.command;

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.messages.command.ITableEditCommand;
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.messages.tableevent.ISchemaChange;
import io.viewserver.messages.tableevent.IStatus;
import io.viewserver.messages.tableevent.ITableEvent;
import io.viewserver.network.Command;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.IInputOperator;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.*;
import io.viewserver.reactor.IReactor;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import gnu.trove.map.hash.TIntIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by nickc on 25/11/2014.
 */
public class TableEditCommandHandler extends CommandHandlerBase<ITableEditCommand> {
    private static final Logger log = LoggerFactory.getLogger(TableEditCommandHandler.class);
    private IDataSourceRegistry dataSourceRegistry;
    private DimensionMapper dimensionMapper;
    private TableFactoryRegistry tableFactoryRegistry;

    public TableEditCommandHandler(IDataSourceRegistry dataSourceRegistry, DimensionMapper dimensionMapper,
                                   TableFactoryRegistry tableFactoryRegistry) {
        super(ITableEditCommand.class);
        this.dataSourceRegistry = dataSourceRegistry;
        this.dimensionMapper = dimensionMapper;
        this.tableFactoryRegistry = tableFactoryRegistry;
    }

    @Override
    protected void handleCommand(Command command, ITableEditCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            String tableName = data.getTableName();
            ITableEditCommand.Operation operation = data.getOperation();
            if (operation == null) {
                operation = ITableEditCommand.Operation.Edit;
            }
            final IOperator operator;
            if (operation == ITableEditCommand.Operation.Create) {
                IOperator existingOperator = peerSession.getSessionCatalog().getOperator(tableName);
                if (existingOperator != null) {
                    throw new Exception(String.format("Operator '%s' already exists - cannot create it again", tableName));
                }

                operator = createTable(data, peerSession.getSystemCatalog(), peerSession.getExecutionContext());
            } else {
                operator = peerSession.getSessionCatalog().getOperator(tableName);
                if (operator == null) {
                    throw new Exception(String.format("Operator '%s' doesn't exist - cannot %s it", tableName, data.getOperation()));
                }

                if (!(operator instanceof ITable)) {
                    throw new Exception("Operator '" + tableName + "' is not a table");
                }
            }

            // TODO: support adding columns after creation
            final ITableEvent tableEvent = data.getTableEvent();
            if (tableEvent.hasSchemaChange()) {
                final ISchemaChange schemaChange = tableEvent.getSchemaChange();
                if (operation == ITableEditCommand.Operation.Create) {
                    if (schemaChange != null) {
                        io.viewserver.schema.Schema schema = ((ITable) operator).getOutput().getSchema();
                        final List<ISchemaChange.IAddColumn> addColumns = schemaChange.getAddColumns();
                        int addCount = addColumns.size();
                        for (int i = 0; i < addCount; i++) {
                            ISchemaChange.IAddColumn column = addColumns.get(i);
                            try {
                                schema.addColumn(ColumnHolderUtils.createColumnHolder(column.getName(),
                                        io.viewserver.schema.column.ColumnType.deserialise(column.getType())));
                            } finally {
                                column.release();
                            }
                        }

                        ((Table) operator).initialise(8);
                    }
                } else {
                    throw new UnsupportedOperationException("Schema changes are not supported after creation");
                }
            }

            final List<IRowEvent> rowEvents = tableEvent.getRowEvents();
            int rowEventCount = rowEvents.size();
            for (int i = 0; i < rowEventCount; i++) {
                final IRowEvent rowEventDto = rowEvents.get(i);
                try {
                    if (!IRowEvent.Type.Add.equals(rowEventDto.getType()) &&
                            rowEventDto.getRowId() < 0) {
                        throw new Exception(String.format("Missing rowId for %s row event", rowEventDto.getType()));
                    }
                } finally {
                    rowEventDto.release();
                }
            }

            IDataSource dataSource = dataSourceRegistry.get(tableName);

            boolean willReset = false;
            final List<IStatus> statuses = tableEvent.getStatuses();
            final int statusCount = statuses.size();
            for (int i = 0; i < statusCount; i++) {
                final IStatus status = statuses.get(i);
                final IStatus.StatusId statusId = status.getStatusId();
                try {
                    switch (statusId) {
                        case SchemaReset: {
                            throw new UnsupportedOperationException("Schema reset in table edit not implemented (yet)");
                        }
                        case DataReset: {
                            operator.resetData();
                            willReset = true;
                            break;
                        }
                        default: {
                            log.warn("Unknown status '{}' in table edit", statusId);
                        }
                    }
                } finally {
                    status.release();
                }
            }

            if (willReset && operator instanceof IInputOperator) {
                ((IInputOperator)operator).deferOperation(() -> performRowOperations(data, commandResult, operator, dataSource, peerSession));
            } else {
                performRowOperations(data, commandResult, operator, dataSource, peerSession);
            }
        } catch(Exception e) {
            if (peerSession.shouldLog()) {
                log.error("Failed to handle table edit command", e);
            }
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }

    private IOperator createTable(ITableEditCommand data, ICatalog catalog, ExecutionContext executionContext) {
        ITableEditCommand.ICreationConfig creationConfig = data.getCreationConfig();
        if (creationConfig == null) {
            return new Table(data.getTableName(), executionContext, catalog, new Schema(), new ChunkedColumnStorage(1024));
        }

        // TODO: make table creation dynamic based on creation config
        String tableType = creationConfig.getTableType();
        ITableFactory tableFactory = tableFactoryRegistry.get(tableType);
        if (tableFactory == null) {
            throw new UnsupportedOperationException(String.format("Unknown table type '%s'", tableType));
        }

        throw new UnsupportedOperationException("Needs re-working for new message stuff");

//        Object config;
//        try {
//            //noinspection unchecked
//            Method parseMethod = tableFactory.getProtoConfigDtoClass().getMethod("parseFrom", ByteString.class);
//
//            config = parseMethod.invoke(null, creationConfig.getConfig());
//        } catch (NoSuchMethodException e) {
//            String message = String.format("Failed to find static method parseFrom() on %s",
//                    ((Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getName());
//            throw new ViewServerException(message, e);
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            String message = String.format("Failed to invoke parseFrom() method on %s",
//                    ((Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getName());
//            throw new ViewServerException(message, e);
//        }
//
//        return (IOperator) tableFactory.create(data.getTableName(), executionContext, catalog, new Schema(), tableFactory.getProtoConfigWrapper(config));
    }

    private void performRowOperations(ITableEditCommand data, CommandResult commandResult, final IOperator operator, final IDataSource dataSource, IPeerSession peerSession) {
        TIntIntHashMap rowIdMap = (TIntIntHashMap) operator.getMetadata("rowIdMap");
        final List<IRowEvent> rowEvents = data.getTableEvent().getRowEvents();
        StringBuilder resultMessage = new StringBuilder();

        final int rowEventCount = rowEvents.size();
        for (int i = 0; i < rowEventCount; i++) {
            final IRowEvent rowEventDto = rowEvents.get(i);
            try {
                if (rowEventDto.getType().equals(IRowEvent.Type.Remove)) {
                    int rowId = rowIdMap != null ? rowIdMap.remove(rowEventDto.getRowId()) : rowEventDto.getRowId();
                    ((ITable) operator).removeRow(rowId);
                    continue;
                }

                ITableRowUpdater tableRowUpdater = new ITableRowUpdater() {
                    @Override
                    public void setValues(ITableRow row) {
                        final List<IRowEvent.IColumnValue> columnValues = rowEventDto.getColumnValues();
                        final int columnValueCount = columnValues.size();
                        for (int i = 0; i < columnValueCount; i++) {
                            final IRowEvent.IColumnValue columnValue = columnValues.get(i);
                            try {
                                ColumnHolder columnHolder = ((ITable) operator).getOutput().getSchema().getColumnHolder(columnValue.getColumnId());
                                Dimension dimension = null;
                                if (dataSource != null && dimensionMapper != null) {
                                    dimension = dataSource.getDimension(columnHolder.getName());
                                    if (dimension != null) {
                                        int id = -1;
                                        switch (dimension.getColumn().getType()) {
                                            case Bool: {
                                                id = dimensionMapper.mapBool(dataSource, dimension, columnValue.getBooleanValue());
                                                break;
                                            }
                                            case NullableBool: {
                                                NullableBool value;
                                                if (columnValue.getNullValue()) {
                                                    value = NullableBool.Null;
                                                } else {
                                                    value = columnValue.getBooleanValue() ? NullableBool.True : NullableBool.False;
                                                }
                                                id = dimensionMapper.mapNullableBool(dataSource, dimension, value);
                                                break;
                                            }
                                            case Byte: {
                                                id = dimensionMapper.mapByte(dataSource, dimension, (byte) columnValue.getIntegerValue());
                                                break;
                                            }
                                            case Short: {
                                                id = dimensionMapper.mapShort(dataSource, dimension, (short) columnValue.getIntegerValue());
                                                break;
                                            }
                                            case Int: {
                                                id = dimensionMapper.mapInt(dataSource, dimension, columnValue.getIntegerValue());
                                                break;
                                            }
                                            case Long: {
                                                id = dimensionMapper.mapLong(dataSource, dimension, columnValue.getLongValue());
                                                break;
                                            }
                                            case String: {
                                                id = dimensionMapper.mapString(dataSource, dimension, columnValue.getStringValue());
                                                break;
                                            }
                                        }
                                        switch (dimension.getCardinality()) {
                                            case Boolean: {
                                                row.setBool(columnHolder.getName(), id == 1);
                                                break;
                                            }
                                            case Byte: {
                                                row.setByte(columnHolder.getName(), (byte) (id & 0xff));
                                                break;
                                            }
                                            case Short: {
                                                row.setShort(columnHolder.getName(), (short) (id & 0xffff));
                                                break;
                                            }
                                            case Int: {
                                                row.setInt(columnHolder.getName(), id);
                                                break;
                                            }
                                        }
                                        continue;
                                    }
                                }

                                switch (columnHolder.getType()) {
                                    case Bool: {
                                        row.setBool(columnHolder.getName(), columnValue.getBooleanValue());
                                        break;
                                    }
                                    case NullableBool: {
                                        NullableBool value;
                                        if (columnValue.getNullValue()) {
                                            value = NullableBool.Null;
                                        } else {
                                            value = columnValue.getBooleanValue() ? NullableBool.True : NullableBool.False;
                                        }
                                        row.setNullableBool(columnHolder.getName(), value);
                                        break;
                                    }
                                    case Byte: {
                                        row.setByte(columnHolder.getName(), (byte) columnValue.getIntegerValue());
                                        break;
                                    }
                                    case Short: {
                                        row.setShort(columnHolder.getName(), (short) columnValue.getIntegerValue());
                                        break;
                                    }
                                    case Int: {
                                        row.setInt(columnHolder.getName(), columnValue.getIntegerValue());
                                        break;
                                    }
                                    case Long: {
                                        switch (columnValue.getValueType()) {
                                            case Long: {
                                                row.setLong(columnHolder.getName(), columnValue.getLongValue());
                                                break;
                                            }
                                            case Double: {
                                                row.setLong(columnHolder.getName(), (long) columnValue.getDoubleValue());
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                    case Float: {
                                        switch (columnValue.getValueType()) {
                                            case Float: {
                                                row.setFloat(columnHolder.getName(), columnValue.getFloatValue());
                                                break;
                                            }
                                            case Double: {
                                                row.setFloat(columnHolder.getName(), (float) columnValue.getDoubleValue());
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                    case Double: {
                                        switch (columnValue.getValueType()) {
                                            case Double: {
                                                row.setDouble(columnHolder.getName(), columnValue.getDoubleValue());
                                                break;
                                            }
                                            case Float: {
                                                row.setDouble(columnHolder.getName(), columnValue.getFloatValue());
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                    case String: {
                                        row.setString(columnHolder.getName(), columnValue.getStringValue());
                                        break;
                                    }
                                    default: {
                                        throw new RuntimeException("Invalid column type '" + columnHolder.getType() + "'");
                                    }
                                }
                            } finally {
                                columnValue.release();
                            }
                        }
                    }
                };
                if (rowEventDto.getType().equals(IRowEvent.Type.Add)) {
                    int rowId = ((ITable) operator).addRow(tableRowUpdater);
                    if(commandResult.getMessage().length() > 0)
                        commandResult.setMessage(",");
                    commandResult.setMessage(",A" + rowId);
                    if (rowIdMap != null) {
                        rowIdMap.put(rowEventDto.getRowId(), rowId);
                    }
                } else {
                    int rowId = rowIdMap != null ? rowIdMap.get(rowEventDto.getRowId()) : rowEventDto.getRowId();
                    if(commandResult.getMessage().length() > 0)
                        commandResult.setMessage(",");
                    commandResult.setMessage("U"+rowId);
                    ((ITable) operator).updateRow(rowId, tableRowUpdater);
                }
            } finally {
                rowEventDto.release();
            }
        }

        if (peerSession.shouldLog()) {
            log.debug("{} row events processed for table '{}' - row count = {}", data.getTableEvent().getRowEvents().size(),
                    data.getTableName(), ((ITable) operator).getOutput().getRowCount());
        }

        IReactor reactor = peerSession.getExecutionContext().getReactor();
        reactor.addLoopTask(new Runnable() {
            @Override
            public void run() {
                commandResult.setSuccess(true).setComplete(true);
                reactor.removeLoopTask(this);
            }
        }, 10);
    }
}
