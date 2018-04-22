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
import io.viewserver.core.IExecutionContext;
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
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import gnu.trove.map.hash.TIntIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Created by nickc on 25/11/2014.
 */
public class TableEditCommandHandler extends CommandHandlerBase<ITableEditCommand> {
    private static final Logger log = LoggerFactory.getLogger(TableEditCommandHandler.class);
    private TableFactoryRegistry tableFactoryRegistry;

    public TableEditCommandHandler(TableFactoryRegistry tableFactoryRegistry) {
        super(ITableEditCommand.class);
        this.tableFactoryRegistry = tableFactoryRegistry;
    }

    @Override
    protected void handleCommand(Command command, ITableEditCommand data, IPeerSession peerSession, CommandResult commandResult) {
        try {
            String tableName1 = data.getTableName();
            String tableName = tableName1;
            ITableEditCommand.Operation operation = data.getOperation();
            if (operation == null) {
                operation = ITableEditCommand.Operation.Edit;
            }
            final IOperator operator;
            if (operation == ITableEditCommand.Operation.Create) {
                IOperator existingOperator = peerSession.getSessionCatalog().getOperatorByPath(tableName);
                if (existingOperator != null) {
                    throw new Exception(String.format("Operator '%s' already exists - cannot create it again", tableName));
                }

                operator = createTable(data, peerSession.getSystemCatalog(), peerSession.getExecutionContext());
            } else {
                operator = peerSession.getSessionCatalog().getOperatorByPath(tableName);
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
                    throw new UnsupportedOperationException("SchemaConfig changes are not supported after creation");
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

            boolean willReset = false;
            final List<IStatus> statuses = tableEvent.getStatuses();
            final int statusCount = statuses.size();
            for (int i = 0; i < statusCount; i++) {
                final IStatus status = statuses.get(i);
                final IStatus.StatusId statusId = status.getStatusId();
                try {
                    switch (statusId) {
                        case SchemaReset: {
                            throw new UnsupportedOperationException("SchemaConfig reset in table edit not implemented (yet)");
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

            Iterator iterator = data.getTableEvent().getRowEvents().iterator();
            if (willReset && operator instanceof IInputOperator) {
                ((IInputOperator)operator).deferOperation(() -> performRowOperations(tableName1, iterator, commandResult, operator, peerSession));
            } else {
                performRowOperations(tableName1, iterator, commandResult, operator, peerSession);
            }
        } catch(Exception e) {
            if (peerSession.shouldLog()) {
                log.error("Failed to handle table edit command", e);
            }
            commandResult.setSuccess(false).setMessage(e.getMessage()).setComplete(true);
        }
    }

    private IOperator createTable(ITableEditCommand data, ICatalog catalog, IExecutionContext executionContext) {
        ITableEditCommand.ICreationConfig creationConfig = data.getCreationConfig();
        if (creationConfig == null || creationConfig.getTableType() == null  || "".equals(creationConfig.getTableType())) {
            Table table = new Table(data.getTableName(), executionContext, catalog, new Schema(), new ChunkedColumnStorage(1024));
            table.setAllowDataReset(true);
            return table;
        }

        // TODO: make table creation dynamic based on creation config
        String tableType = creationConfig.getTableType();
        ITableFactory tableFactory = tableFactoryRegistry.get(tableType);
        if (tableFactory == null) {
            throw new UnsupportedOperationException(String.format("Unknown table type '%s'", tableType));
        }
        return (IOperator) tableFactory.create(data.getTableName(),executionContext, catalog,new Schema(),creationConfig);
        //throw new UnsupportedOperationException("Needs re-working for new message stuff");

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
//        return (IOperator) tableFactory.create(data.getTableName(), executionContext, catalog, new SchemaConfig(), tableFactory.getProtoConfigWrapper(config));
    }

    private void performRowOperations(String tableName,Iterator<IRowEvent> rowEvents,CommandResult commandResult, final IOperator operator, IPeerSession peerSession) {
        TIntIntHashMap rowIdMap = (TIntIntHashMap) operator.getMetadata("rowIdMap");
        int rowEventCount = 0;
        while (rowEvents.hasNext()) {
            final IRowEvent rowEventDto = rowEvents.next();
            rowEventCount++;
            try {
                if (rowEventDto.getType().equals(IRowEvent.Type.Remove)) {
                    if(operator instanceof KeyedTable && rowEventDto.getKey() != null) {
                        TableKey removeKey = new TableKey(rowEventDto.getKey());
                        ((KeyedTable) operator).removeRow(removeKey);
                        commandResult.setMessage("R "+ rowEventDto.getKey());
                    }else{
                        int rowId = rowIdMap != null ? rowIdMap.get(rowEventDto.getRowId()) : rowEventDto.getRowId();
                        ((ITable) operator).removeRow(rowId);
                        commandResult.setMessage("R "+ rowId);
                    }
                    continue;
                }

                ITableRowUpdater tableRowUpdater = new RowUpdater();
                if (rowEventDto.getType().equals(IRowEvent.Type.Add)) {
                    int rowId = ((ITable) operator).addRow(tableRowUpdater);
                    if(commandResult.getMessage().length() > 0)
                        commandResult.setMessage(",");
                    commandResult.setMessage("A" + rowId);
                    if (rowIdMap != null) {
                        rowIdMap.put(rowEventDto.getRowId(), rowId);
                    }
                } else {

                    if(commandResult.getMessage().length() > 0)
                        commandResult.setMessage(",");

                    if(operator instanceof KeyedTable && rowEventDto.getKey() != null) {
                        TableKey updateKey = new TableKey(rowEventDto.getKey());
                        ((KeyedTable) operator).updateRow(updateKey, tableRowUpdater);
                        commandResult.setMessage("U "+ rowEventDto.getKey());
                    }else{
                        int rowId = rowIdMap != null ? rowIdMap.get(rowEventDto.getRowId()) : rowEventDto.getRowId();
                        ((ITable) operator).updateRow(rowId, tableRowUpdater);
                        commandResult.setMessage("U "+ rowId);
                    }
                }
            } finally {
                rowEventDto.release();
            }
        }

        if (peerSession.shouldLog()) {
            log.debug("{} row events processed for table '{}' - row count = {}", rowEventCount,
                    tableName, ((ITable) operator).getOutput().getRowCount());
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