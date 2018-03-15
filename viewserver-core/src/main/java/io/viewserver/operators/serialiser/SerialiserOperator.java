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

package io.viewserver.operators.serialiser;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.SubscriptionManager;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.datasource.ColumnType;
import io.viewserver.execution.Options;
import io.viewserver.execution.TableMetaData;
import io.viewserver.messages.IMessage;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.messages.tableevent.ISchemaChange;
import io.viewserver.messages.tableevent.IStatus;
import io.viewserver.messages.tableevent.ITableEvent;
import io.viewserver.network.IMessageManager;
import io.viewserver.operators.*;
import io.viewserver.schema.column.*;
import io.viewserver.subscription.SubscriptionFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nickc on 06/10/2014.
 */
public class SerialiserOperator extends OperatorBase {
    private static final Logger log = LoggerFactory.getLogger(SerialiserOperator.class);
    private final Input input;
    private final boolean isSnapshotOnly;
    private IMessageManager messageManager;
    private SubscriptionManager subscriptionManager;
    private int connectionId;
    private int commandId;
    private boolean snapshotComplete;
    private IMessage pendingMessage;
    private HashMap<Status,Status> statusWorkFlow = new HashMap<>();
    private List<Status> alreadySentStatus = new ArrayList<>();

    public SerialiserOperator(String name, IExecutionContext executionContext, ICatalog catalog,
                              IMessageManager messageManager, SubscriptionManager subscriptionManager, int connectionId,
                              int commandId, Options options) {
        super(name, executionContext, catalog);
        this.messageManager = messageManager;
        this.subscriptionManager = subscriptionManager;
        this.connectionId = connectionId;
        this.commandId = commandId;

        input = new Input(Constants.IN, this);
        addInput(input);

        subscriptionManager.registerSubscription(connectionId, commandId, this);

        isSnapshotOnly = options != null && options.hasFlag(SubscriptionFlags.SnapshotOnly);
    }

    public IInput getInput() {
        return input;
    }

    @Override
    protected void commit() {
        if (log.isTraceEnabled()) {
            log.trace("Beginning commit of serialiser {}", commandId);
        }
        super.commit();

        if (pendingMessage != null) {
            messageManager.sendMessage(pendingMessage);
            pendingMessage.release();
            pendingMessage = null;
        }
    }

    @Override
    protected void propagateStatus(Status status) {
        this.input.addStatus(status);
    }

    @Override
    public void doTearDown() {
        subscriptionManager.unregisterSubscription(connectionId, commandId);
        super.doTearDown();
    }

    private class Input extends InputBase {
        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        protected void onSchemaReset() {
            if (snapshotComplete && isSnapshotOnly) {
                return;
            }
            addStatus(Status.SchemaReset);
            super.onSchemaReset();
        }

        @Override
        protected void onColumnAdd(ColumnHolder columnHolder) {
            if (snapshotComplete && isSnapshotOnly) {
                return;
            }
            ISchemaChange schemaChange = ensureSchemaChange();
            ISchemaChange.IAddColumn addColumn = MessagePool.getInstance().get(ISchemaChange.IAddColumn.class)
                    .setColumnId(columnHolder.getColumnId())
                    .setName(columnHolder.getName())
                    .setType(columnHolder.getType().serialise());
            ColumnMetadata metadata = columnHolder.getMetadata();
            if (metadata != null) {
                if (metadata.getDataType() != null) {
                    addColumn.setDataType(serialiseDataType(metadata.getDataType()));
                }
                switch (columnHolder.getType()) {
                    case Bool: {
                        addColumn.setBooleanNullValue(((ColumnMetadataBool) metadata).getNullValue());
                        break;
                    }
                    case NullableBool: {
                        addColumn.setNullNullValue();
                        break;
                    }
                    case Byte: {
                        addColumn.setIntegerNullValue(((ColumnMetadataByte) metadata).getNullValue());
                        break;
                    }
                    case Short: {
                        addColumn.setIntegerNullValue(((ColumnMetadataShort) metadata).getNullValue());
                        break;
                    }
                    case Int: {
                        addColumn.setIntegerNullValue(((ColumnMetadataInt) metadata).getNullValue());
                        break;
                    }
                    case Long: {
                        addColumn.setLongNullValue(((ColumnMetadataLong) metadata).getNullValue());
                        break;
                    }
                    case Float: {
                        addColumn.setFloatNullValue(((ColumnMetadataFloat) metadata).getNullValue());
                        break;
                    }
                    case Double: {
                        addColumn.setDoubleNullValue(((ColumnMetadataDouble) metadata).getNullValue());
                        break;
                    }
                    case String: {
                        ColumnMetadataString stringMetadata = (ColumnMetadataString) metadata;
                        if (stringMetadata.getNullValue() == null) {
                            addColumn.setNullNullValue();
                        } else {
                            addColumn.setStringNullValue(stringMetadata.getNullValue());
                        }
                        break;
                    }
                    default: {
                        throw new RuntimeException("Unknown column type '" + columnHolder.getType() + "'");
                    }
                }
            }
            schemaChange.getAddColumns().add(addColumn);
            addColumn.release();
        }

        private ISchemaChange.DataType serialiseDataType(ColumnType dataType) {
            switch (dataType) {
                case Bool: {
                    return ISchemaChange.DataType.Boolean;
                }
                case NullableBool: {
                    return ISchemaChange.DataType.NullableBoolean;
                }
                case Byte: {
                    return ISchemaChange.DataType.Byte;
                }
                case Short: {
                    return ISchemaChange.DataType.Short;
                }
                case Int: {
                    return ISchemaChange.DataType.Integer;
                }
                case Long: {
                    return ISchemaChange.DataType.Long;
                }
                case Float: {
                    return ISchemaChange.DataType.Float;
                }
                case Double: {
                    return ISchemaChange.DataType.Double;
                }
                case String: {
                    return ISchemaChange.DataType.String;
                }
                case Date: {
                    return ISchemaChange.DataType.Date;
                }
                case DateTime: {
                    return ISchemaChange.DataType.DateTime;
                }
                default: {
                    throw new IllegalArgumentException("Unknown data type '" + dataType + "'");
                }
            }
        }

        @Override
        protected void onDataReset() {
            if (snapshotComplete && isSnapshotOnly) {
                return;
            }
            log.debug("Data reset on serialiser " + commandId);
            addStatus(Status.DataReset);
            super.onDataReset();
            snapshotComplete = true;
            setFlag(TableEventFlags.SnapshotComplete);
        }

        @Override
        protected void onRowAdd(int row) {
            log.debug("Row added on serialiser {}",commandId);
            if (snapshotComplete && isSnapshotOnly) {
                return;
            }
            addRowEvent(IRowEvent.Type.Add, row, null);
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            log.debug("Row updated on serialiser {}",commandId);
            if (snapshotComplete && isSnapshotOnly) {
                return;
            }
            addRowEvent(IRowEvent.Type.Update, row, rowFlags);
        }

        @Override
        protected void onRowRemove(int row) {
            if (snapshotComplete && isSnapshotOnly) {
                return;
            }
            ensureMessageBuilder();
            final IRowEvent rowEvent = MessagePool.getInstance().get(IRowEvent.class)
                    .setType(IRowEvent.Type.Remove)
                    .setRowId(row);
            pendingMessage.getTableEvent().getRowEvents().add(rowEvent);
            rowEvent.release();
        }

        private void ensureMessageBuilder() {
            if (pendingMessage == null) {
                TableMetaData metaData = getProducer().getMetaData();

                pendingMessage = MessagePool.getInstance().get(IMessage.class);
                final ITableEvent tableEvent = pendingMessage.getTableEvent();
                tableEvent.setId(commandId)
                        .setInitSize(getProducer().getRowCount())
                        .setExecutionCycle(getExecutionContext().getExecutionCount())
                        .setMetadata(metaData.toMessage());
            }
        }

        private ISchemaChange ensureSchemaChange() {
            ensureMessageBuilder();
            ITableEvent tableEvent = pendingMessage.getTableEvent();
            final ISchemaChange schemaChange = tableEvent.getSchemaChange();
            schemaChange.setSchemaSize(getProducer().getSchema().getWidth());
            return tableEvent.getSchemaChange();
        }

        private void setFlag(TableEventFlags flagsToSet) {
            final ITableEvent tableEvent = pendingMessage.getTableEvent();
            tableEvent.setFlags(tableEvent.getFlags() | flagsToSet.getFlags().getNumber());
        }

        private IRowEvent createRowEvent(IRowEvent.Type eventType, int row) {
            return MessagePool.getInstance().get(IRowEvent.class)
                    .setType(eventType)
                    .setRowId(row);
        }




        private void addStatus(Status status) {
            ensureMessageBuilder();
            final IStatus statusId = MessagePool.getInstance().get(IStatus.class).setStatusId(status.serialise());
            pendingMessage.getTableEvent().getStatuses().add(statusId);
            statusId.release();
        }

        private void addRowEvent(IRowEvent.Type eventType, int row, IRowFlags rowFlags) {
            ensureMessageBuilder();
            IRowEvent rowEvent = createRowEvent(eventType, row);
            List<ColumnHolder> columnHolders = getProducer().getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                if (rowFlags != null && !rowFlags.isDirty(columnHolder.getColumnId())) {
                    continue;
                }
                IRowEvent.IColumnValue columnValue = MessagePool.getInstance().get(IRowEvent.IColumnValue.class)
                        .setColumnId(columnHolder.getColumnId());

                ColumnMetadata metadata = columnHolder.getMetadata();

                switch (columnHolder.getType()) {
                    case Bool: {
                        boolean booleanValue = ((IColumnBool) columnHolder).getBool(row);

                        if (metadata == null || booleanValue != ((ColumnMetadataBool) metadata).getNullValue()) {
                            columnValue.setBooleanValue(booleanValue);
                        } else {
                            columnValue.setNullValue();
                        }

                        break;
                    }
                    case NullableBool: {
                        NullableBool nullableBoolValue = ((IColumnNullableBool) columnHolder).getNullableBool(row);

                        if (nullableBoolValue != NullableBool.Null) {
                            columnValue.setBooleanValue(nullableBoolValue.getBooleanValue());
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    case Byte: {
                        byte byteValue = ((IColumnByte) columnHolder).getByte(row);

                        if (metadata == null || byteValue != ((ColumnMetadataByte) metadata).getNullValue()) {
                            columnValue.setIntegerValue(byteValue);
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    case Short: {
                        short shortValue = ((IColumnShort) columnHolder).getShort(row);

                        if (metadata == null || shortValue != ((ColumnMetadataShort) metadata).getNullValue()) {
                            columnValue.setIntegerValue(shortValue);
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    case Int: {
                        int intValue = ((IColumnInt) columnHolder).getInt(row);

                        if (metadata == null || intValue != ((ColumnMetadataInt) metadata).getNullValue()) {
                            columnValue.setIntegerValue(intValue);
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    case Long: {
                        long longValue = ((IColumnLong) columnHolder).getLong(row);

                        if (metadata == null || longValue != ((ColumnMetadataLong) metadata).getNullValue()) {
                            columnValue.setLongValue(longValue);
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    case Float: {
                        float floatValue = ((IColumnFloat) columnHolder).getFloat(row);

                        if (metadata == null || floatValue != ((ColumnMetadataFloat) metadata).getNullValue()) {
                            columnValue.setFloatValue(floatValue);
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    case Double: {
                        double doubleValue = ((IColumnDouble) columnHolder).getDouble(row);

                        if (metadata == null || doubleValue != ((ColumnMetadataDouble) metadata).getNullValue()) {
                            columnValue.setDoubleValue(doubleValue);
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    case String: {
                        String stringValue = ((IColumnString) columnHolder).getString(row);

                        String nullValue = metadata != null ? ((ColumnMetadataString) metadata).getNullValue() : null;
                        if ((metadata == null && stringValue != null)
                                || (metadata != null && !(stringValue == null ? nullValue == null : stringValue.equals(nullValue)))) {
                            columnValue.setStringValue(stringValue);
                        } else {
                            columnValue.setNullValue();
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Cannot serialise type " + columnHolder.getType());
                    }
                }
                rowEvent.getColumnValues().add(columnValue);
                columnValue.release();
            }
            log.debug("Added row event on serialiser {}",commandId);
            pendingMessage.getTableEvent().getRowEvents().add(rowEvent);
            rowEvent.release();
        }
    }
}
