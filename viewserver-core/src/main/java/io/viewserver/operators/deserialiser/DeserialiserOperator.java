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

package io.viewserver.operators.deserialiser;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.collections.IntHashSet;
import io.viewserver.command.CommandResult;
import io.viewserver.command.ICommandResultListener;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.execution.TableMetaData;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.ISubscribeCommand;
import io.viewserver.messages.command.IUnsubscribeCommand;
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.messages.tableevent.ISchemaChange;
import io.viewserver.messages.tableevent.IStatus;
import io.viewserver.messages.tableevent.ITableEvent;
import io.viewserver.network.Command;
import io.viewserver.network.IDataHandler;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.*;
import io.viewserver.operators.serialiser.TableEventFlags;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by nickc on 07/10/2014.
 */
public class DeserialiserOperator extends InputOperatorBase implements IDataHandler, ICommandResultListener {
    private static final Logger log = LoggerFactory.getLogger(DeserialiserOperator.class);
    private Output output;
    private IPeerSession peerSession;
    private String target;
    private ITableStorage tableStorage;
    private IntHashSet rowMapping = new IntHashSet(128, 0.75f, -1);
    private int[] columnMapping;
    private List<IDeserialiserEventHandler> eventHandlers;
    private Command subscribeCommand;
    private Command updateCommand;
    private boolean connected;
    private boolean snapshotComplete;
    private final List<ITableEvent> pendingTableEvents = new ArrayList<>();
    private final List<ITableEvent> pendingTableEventsCopy = new ArrayList<>();

    public DeserialiserOperator(String name, ExecutionContext executionContext, ICatalog catalog, IPeerSession peerSession, String target, ITableStorage tableStorage) {
        super(name, executionContext, catalog);
        this.peerSession = peerSession;
        this.target = target;
        this.tableStorage = tableStorage;

        output = new Output(Constants.OUT, this);
        addOutput(output);

        peerSession.registerDeserialiser(this);
    }

    public void connect() {
        if (this.subscribeCommand == null) {
            ISubscribeCommand command = MessagePool.getInstance().get(ISubscribeCommand.class)
                    .setOperatorName(target)
                    .setOutputName(Constants.OUT);
            this.subscribeCommand = new Command("subscribe", command);
        }
        subscribeCommand.setContinuous(true);
        subscribeCommand.setDataHandler(this);
        subscribeCommand.setCommandResultListener(this);

        peerSession.sendCommand(subscribeCommand);
        connected = true;
    }

    public void update() {
        if (this.updateCommand == null) {
            log.error("An update command must be set before calling update()");
            return;
        }

        peerSession.sendCommand(updateCommand);
    }

    @Override
    protected void commit() {
        pendingTableEventsCopy.clear();
        pendingTableEventsCopy.addAll(pendingTableEvents);
        pendingTableEvents.clear();
        int count = pendingTableEventsCopy.size();
        for (int i = 0; i < count; i++) {
            processTableEvent(pendingTableEventsCopy.get(i));
        }

        super.commit();
    }

    @Override
    public void doTearDown() {
        super.doTearDown();
        if (connected) {
            if (!peerSession.isTornDown()) {
                peerSession.closeCommand(subscribeCommand);
                subscribeCommand.getMessage().release();
                subscribeCommand = null;

                if (updateCommand != null) {
                    updateCommand.getMessage().release();
                    updateCommand = null;
                }

                IUnsubscribeCommand unsubscribeCommand = MessagePool.getInstance().get(IUnsubscribeCommand.class)
                        .setSubscriptionId(subscribeCommand.getId());
                peerSession.sendCommand(new Command("unsubscribe", unsubscribeCommand));
                unsubscribeCommand.release();

                connected = false;

                peerSession.unregisterDeserialiser(this);
            }
        }
    }

    @Override
    public void handleTableEvent(ITableEvent tableEvent) {
//        pendingTableEvents.add(tableEvent);
        processTableEvent(tableEvent);
    }

    private void processTableEvent(ITableEvent tableEvent) {
        output.setMetaData(TableMetaData.fromDto(tableEvent.getMetadata()));

        List<IStatus> statusesList = tableEvent.getStatuses();
        int count = statusesList.size();
        for (int i = 0; i < count; i++) {
            IStatus status = statusesList.get(i);
            switch (status.getStatusId()) {
                case SchemaReset: {
                    doResetSchema();
                    break;
                }
                case DataReset: {
                    doResetData();
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown status " + status.getStatusId());
                }
            }
            output.getCurrentChanges().handleStatus(Status.fromDto(status.getStatusId()));
        }

        if (tableEvent.hasSchemaChange()) {
            handleSchemaChange(tableEvent.getSchemaChange());
        }

        List<IRowEvent> rowEventsList = tableEvent.getRowEvents();
        count = rowEventsList.size();
        for (int i = 0; i < count; i++) {
            IRowEvent rowEvent = rowEventsList.get(i);
            switch (rowEvent.getType()) {
                case Add: {
                    onRowAdd(rowEvent);
                    break;
                }
                case Update: {
                    onRowUpdate(rowEvent);
                    break;
                }
                case Remove: {
                    onRowRemove(rowEvent);
                    break;
                }
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("{} - Processed {} row events", getName(), tableEvent.getRowEvents().size());
        }

        snapshotComplete = (tableEvent.getFlags() & TableEventFlags.SnapshotComplete.getFlags().getNumber()) != 0;
        fireEvents((handler) -> {
            handler.onBatchComplete(this, tableEvent.getRowEvents().size());
            if (snapshotComplete) {
                handler.onSnapshotComplete(this);
            }
        });
    }

    private void fireEvents(Consumer<IDeserialiserEventHandler> consumer) {
        if (eventHandlers != null) {
            int count = eventHandlers.size();
            for (int i = 0; i < count; i++) {
                consumer.accept(eventHandlers.get(i));
            }
        }
    }

    private void doResetSchema() {
        log.trace("{} - resetting schema", getName());
        if (output.getSchema() != null) {
            output.clearSchema();
        }
        output.resetSchema();
        doResetData();
    }

    private void doResetData() {
        log.trace("{} - resetting data", getName());
        fireEvents((handler) -> handler.onDataReset(this));

        output.resetData();
        output.clearData();

        output.getCurrentChanges().clear();
        rowMapping.clear();
        if (output.schema != null) {
            List<ColumnHolder> columnHolders = output.schema.getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                ((IWritableColumn) columnHolder.getColumn()).resetAll();
            }
        }
    }

    private void handleSchemaChange(ISchemaChange schemaChange) {
        if (output.schema == null) {
            output.schema = new Schema();
        }

        columnMapping = new int[schemaChange.getSchemaSize()];

        List<ISchemaChange.IAddColumn> addedColumnsList = schemaChange.getAddColumns();
        int count = addedColumnsList.size();
        for (int i = 0; i < count; i++) {
            ISchemaChange.IAddColumn addedColumn = addedColumnsList.get(i);

            ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder(addedColumn.getName(), ColumnType.deserialise(addedColumn.getType()));
            this.createMetaData(columnHolder, columnHolder.getType(), addedColumn);

            output.getSchema().addColumn(columnHolder);
            columnMapping[addedColumn.getColumnId()] = columnHolder.getColumnId();

            addedColumn.release();
        }

        tableStorage.initialise(128, output.getSchema(), output.getCurrentChanges());
    }

    private void createMetaData(ColumnHolder columnHolder, ColumnType type, ISchemaChange.IAddColumn addedColumn) {
        ColumnMetadata metadata = ColumnHolderUtils.createColumnMetadata(type);
        ISchemaChange.DataType dataType = addedColumn.getDataType();
        if (dataType != null) {
            metadata.setDataType(deserialiseDataType(dataType));
        }
        switch (type) {
            case Bool: {
                ((ColumnMetadataBool)metadata).setNullValue(addedColumn.getBooleanNullValue());
                break;
            }
            case NullableBool: {
                break;
            }
            case Byte: {
                ((ColumnMetadataByte)metadata).setNullValue((byte) addedColumn.getIntegerNullValue());
                break;
            }
            case Short: {
                ((ColumnMetadataShort)metadata).setNullValue((short) addedColumn.getIntegerNullValue());
                break;
            }
            case Int: {
                ((ColumnMetadataInt)metadata).setNullValue(addedColumn.getIntegerNullValue());
                break;
            }
            case Long: {
                ((ColumnMetadataLong)metadata).setNullValue(addedColumn.getLongNullValue());
                break;
            }
            case Float: {
                ((ColumnMetadataFloat)metadata).setNullValue(addedColumn.getFloatNullValue());
                break;
            }
            case Double: {
                ((ColumnMetadataDouble)metadata).setNullValue(addedColumn.getDoubleNullValue());
                break;
            }
            case String: {
                if (addedColumn.getStringNullValue() != null) {
                    ((ColumnMetadataString)metadata).setNullValue(addedColumn.getStringNullValue());
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown column type '" + type + "'");
            }
        }
        columnHolder.setMetadata(metadata);
    }

    private io.viewserver.datasource.ColumnType deserialiseDataType(ISchemaChange.DataType dataType) {
        switch (dataType) {
            case Boolean: { return io.viewserver.datasource.ColumnType.Bool; }
            case NullableBoolean: { return io.viewserver.datasource.ColumnType.NullableBool; }
            case Byte: { return io.viewserver.datasource.ColumnType.Byte; }
            case Short: { return io.viewserver.datasource.ColumnType.Short; }
            case Integer: { return io.viewserver.datasource.ColumnType.Int; }
            case Long: { return io.viewserver.datasource.ColumnType.Long; }
            case Float: { return io.viewserver.datasource.ColumnType.Float; }
            case Double: { return io.viewserver.datasource.ColumnType.Double; }
            case String: { return io.viewserver.datasource.ColumnType.String; }
            case Date: { return io.viewserver.datasource.ColumnType.Date; }
            case DateTime: { return io.viewserver.datasource.ColumnType.DateTime; }
            default: { throw new IllegalArgumentException("Unknown data type '" + dataType + "'"); }
        }
    }

    private void onRowAdd(IRowEvent rowEvent) {
        int rowId = rowMapping.addInt(rowEvent.getRowId());
        if (rowId < 0) {
            rowId = -rowId - 1;
            if (output.isRowActive(rowId)) {
                throw new IllegalStateException("Adding row " + rowEvent.getRowId() + " when it already exists");
            }
        }
        tableStorage.ensureCapacity(rowId + 1, output.getSchema());
        updateColumnValues(rowId, rowEvent.getColumnValues());
        output.handleAdd(rowId);

        if (eventHandlers != null) {
            int count = eventHandlers.size();
            for (int i = 0; i < count; i++) {
                eventHandlers.get(i).onRowAdd(this, rowId);
            }
        }
    }

    private void updateColumnValues(int rowId, List<IRowEvent.IColumnValue> values) {
        int count = values.size();
        for (int i = 0; i < count; i++) {
            final IRowEvent.IColumnValue columnValue = values.get(i);
            int columnId = columnMapping[columnValue.getColumnId()];
            ColumnHolder columnHolder = output.getSchema().getColumnHolder(columnId);
            ColumnMetadata metadata = columnHolder.getMetadata();

            switch (columnHolder.getType()) {
                case Bool: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnBool) columnHolder.getColumn()).setBool(rowId, ((ColumnMetadataBool) metadata).getNullValue());
                    } else {
                        ((IWritableColumnBool) columnHolder.getColumn()).setBool(rowId, columnValue.getBooleanValue());
                    }
                    break;
                }
                case NullableBool: {
                    if (columnValue.getNullValue()) {
                        ((IWritableColumnNullableBool) columnHolder.getColumn()).setNullableBool(rowId, NullableBool.Null);
                    } else {
                        ((IWritableColumnNullableBool)columnHolder.getColumn()).setNullableBool(rowId, NullableBool.fromBoolean(columnValue.getBooleanValue()));
                    }
                    break;
                }
                case Byte: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnByte) columnHolder.getColumn()).setByte(rowId, ((ColumnMetadataByte) metadata).getNullValue());
                    } else {
                        ((IWritableColumnByte) columnHolder.getColumn()).setByte(rowId, (byte) (columnValue.getIntegerValue() & 0xff));
                    }
                    break;
                }
                case Short: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnShort) columnHolder.getColumn()).setShort(rowId, ((ColumnMetadataShort) metadata).getNullValue());
                    } else {
                        ((IWritableColumnShort) columnHolder.getColumn()).setShort(rowId, (short) (columnValue.getIntegerValue() & 0xffff));
                    }
                    break;
                }
                case Int: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnInt) columnHolder.getColumn()).setInt(rowId, ((ColumnMetadataInt) metadata).getNullValue());
                    } else {
                        ((IWritableColumnInt) columnHolder.getColumn()).setInt(rowId, columnValue.getIntegerValue());
                    }
                    break;
                }
                case Long: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnLong) columnHolder.getColumn()).setLong(rowId, ((ColumnMetadataLong) metadata).getNullValue());
                    } else {
                        ((IWritableColumnLong) columnHolder.getColumn()).setLong(rowId, columnValue.getLongValue());
                    }
                    break;
                }
                case Float: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnFloat) columnHolder.getColumn()).setFloat(rowId, ((ColumnMetadataFloat) metadata).getNullValue());
                    } else {
                        ((IWritableColumnFloat) columnHolder.getColumn()).setFloat(rowId, columnValue.getFloatValue());
                    }
                    break;
                }
                case Double: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnDouble) columnHolder.getColumn()).setDouble(rowId, ((ColumnMetadataDouble) metadata).getNullValue());
                    } else {
                        ((IWritableColumnDouble) columnHolder.getColumn()).setDouble(rowId, columnValue.getDoubleValue());
                    }
                    break;
                }
                case String: {
                    if (columnValue.getNullValue() && metadata != null) {
                        ((IWritableColumnString) columnHolder.getColumn()).setString(rowId, ((ColumnMetadataString) metadata).getNullValue());
                    } else {
                        writeStringValue(columnValue.getStringValue(), columnHolder, rowId);
                    }
                    break;
                }
            }
        }
    }

    protected void writeStringValue(String value, ColumnHolder columnHolder, int rowId) {
        ((IWritableColumnString) columnHolder.getColumn()).setString(rowId, value);
    }

    private void onRowUpdate(IRowEvent rowEvent) {
        int index = rowMapping.index(rowEvent.getRowId());
        if (index < 0) {
            throw new IllegalStateException("Cannot update row " + rowEvent.getRowId() + " as it does not exist");
        }
        int rowId = rowMapping.get(index);
        if (!output.isRowActive(rowId)) {
            throw new IllegalStateException("Cannot update row " + rowEvent.getRowId() + " as it does not exist");
        }
        updateColumnValues(rowId, rowEvent.getColumnValues());
        output.handleUpdate(rowId);

        if (eventHandlers != null) {
            TableEventRowFlagWrapper rowFlags = new TableEventRowFlagWrapper(rowEvent.getColumnValues());
            int count = eventHandlers.size();
            for (int i = 0; i < count; i++) {
                eventHandlers.get(i).onRowUpdate(this, rowId, rowFlags);
            }
        }
    }

    protected void onRowRemove(IRowEvent rowEvent) {
        int index = rowMapping.index(rowEvent.getRowId());
        if (index < 0) {
            throw new IllegalStateException("Cannot remove row " + rowEvent.getRowId() + " as it does not exist");
        }
        int rowId = rowMapping.get(index);
        if (!output.isRowActive(rowId)) {
            throw new IllegalStateException("Cannot remove row " + rowEvent.getRowId() + " as it does not exist");
        }
        output.handleRemove(rowId);

        if (eventHandlers != null) {
            int count = eventHandlers.size();
            for (int i = 0; i < count; i++) {
                eventHandlers.get(i).onRowRemove(this, rowId);
            }
        }
    }

    public IOutput getOutput() {
        return output;
    }

    public List<IDeserialiserEventHandler> getEventHandlers() {
        return eventHandlers;
    }

    public void addEventHandler(IDeserialiserEventHandler eventHandler) {
        if (eventHandlers == null) {
            eventHandlers = new ArrayList<>();
        }
        eventHandlers.add(eventHandler);
        if (snapshotComplete) {
            eventHandler.onSnapshotComplete(this);
        }
    }

    public void removeEventHandler(IDeserialiserEventHandler eventHandler) {
        if (eventHandlers != null) {
            eventHandlers.remove(eventHandler);
        }
    }

    public IPeerSession getPeerSession() {
        return peerSession;
    }

    protected String getTarget() {
        return target;
    }

    public Command getSubscribeCommand() {
        return subscribeCommand;
    }

    public void setSubscribeCommand(Command subscribeCommand) {
        this.subscribeCommand = subscribeCommand;
        subscribeCommand.getMessage().retain();
    }

    public void setUpdateCommand(Command updateCommand) {
        this.updateCommand = updateCommand;
    }

    @Override
    public void onResult(CommandResult commandResult) {

        if(commandResult.isComplete()) {
            if(!commandResult.isSuccess()) {
                fireEvents((handler) -> handler.onSubscriptionError(this, commandResult.getMessage()));
            }else{
                fireEvents((handler) -> handler.onSubscriptionSuccess(this));
            }
        }
    }

    private class Output extends OutputBase {
        private Schema schema;

        public Output(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public void commit() {
            if (schema == null) {
                return;
            }

            super.commit();
        }
    }

    private class TableEventRowFlagWrapper implements IRowFlags {
        private List<IRowEvent.IColumnValue> values;

        private TableEventRowFlagWrapper(List<IRowEvent.IColumnValue> values) {
            this.values = values;
        }

        @Override
        public boolean isDirty(int columnId) {
            int count = values.size();
            for (int i = 0; i < count; i++) {
                if (values.get(i).getColumnId() == columnId) {
                    return true;
                }
            }

            return false;
        }
    }
}
