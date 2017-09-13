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

package io.viewserver.distribution;

import io.viewserver.Constants;
import io.viewserver.command.CommandResult;
import io.viewserver.command.MultiCommandResult;
import io.viewserver.core.NullableBool;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.ITableEditCommand;
import io.viewserver.messages.tableevent.IRowEvent;
import io.viewserver.messages.tableevent.ITableEvent;
import io.viewserver.network.Command;
import io.viewserver.schema.column.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by nickc on 09/12/2014.
 */
public abstract class DataDistributorBase implements IDataDistributor {
    private static final Logger log = LoggerFactory.getLogger(DataDistributorBase.class);
    protected DistributionOperator owner;
    private IDistributionManager distributionManager;
    protected Map<ViewServerNode, ITableEvent> tableEvents = new HashMap<>();
    private MultiCommandResult tableEventsResult;
    private CommandResult placeholderResult;

    protected DataDistributorBase(DistributionOperator owner, IDistributionManager distributionManager) {
        this.owner = owner;
        this.distributionManager = distributionManager;
    }

    protected ITableEvent getNodeTableEvent(ViewServerNode node) {
        ITableEvent tableEvent = tableEvents.get(node);
        if (tableEvent == null) {
            tableEvent = MessagePool.getInstance().get(ITableEvent.class)
                    .setId(0)
                    .setInitSize(0)
                    .setExecutionCycle(0);
            tableEvents.put(node, tableEvent);
        }
        return tableEvent;
    }

    @Override
    public void setTableEventsResult(CommandResult commandResult) {
        tableEventsResult = MultiCommandResult.wrap("tableEventsResult", commandResult);
        placeholderResult = tableEventsResult.getResultForDependency("placeholder");
    }

    @Override
    public DistributionOperator getOwner() {
        return owner;
    }

    @Override
    public void onBeforeCommit() {
    }

    @Override
    public void onAfterCommit() {
        tableEventsResult = null;
        placeholderResult = null;
        sendTableEvents();
    }

    @Override
    public void sendTableEvents() {
        tableEvents.entrySet().forEach(sendTableEventProc);

        tableEvents.clear();

        onSendTableEventsComplete();
    }

    private final Consumer<Map.Entry<ViewServerNode, ITableEvent>> sendTableEventProc = entry -> {
        ViewServerNode node = entry.getKey();
        ITableEvent tableEvent = entry.getValue();
        if (log.isTraceEnabled()) {
            log.trace("Sending table event with {} rows to {}", tableEvent.getRowEvents().size(), node);
        }
        sendTableEvent(node, tableEvent);
        tableEvent.release();
    };

    protected void onSendTableEventsComplete() {
        if (placeholderResult != null) {
            placeholderResult.setSuccess(true).setComplete(true);
        }
    }

    protected void sendTableEvent(ViewServerNode node, ITableEvent tableEvent) {
        if (!tableEvent.getRowEvents().isEmpty()) {
            ITableEditCommand commandDto = MessagePool.getInstance().get(ITableEditCommand.class)
                    .setTableName(owner.getInput(Constants.IN).getProducer().getOwner().getPath())
                    .setOperation(ITableEditCommand.Operation.Edit)
                    .setTableEvent(tableEvent);

            Command command = new Command("tableEdit", commandDto);
            if (tableEventsResult != null) {
                CommandResult tableEventResult = this.tableEventsResult.getResultForDependency(UUID.randomUUID().toString());
                command.setCommandResultListener(commandResult -> tableEventResult.setSuccess(commandResult.isSuccess())
                        .setMessage(commandResult.getMessage())
                        .setComplete(commandResult.isComplete()));
            }
            node.getPeerSession().sendCommand(command);
            commandDto.release();
        }
    }

    @Override
    public void reset() {
        owner.resetData();
    }

    @Override
    public void tearDown() {
    }

    @Override
    public void addRowEvent(ViewServerNode node, IRowEvent.Type eventType, int row, IRowFlags rowFlags) {
        ITableEvent tableEvent = getNodeTableEvent(node);
        addRowEvent(tableEvent, eventType, row, rowFlags);
        if (tableEvent.getRowEvents().size() >= 10000) {
            log.trace("10000 row events - sending to node {}", node);
            sendTableEvent(node, tableEvent);
            tableEvent.release();
            tableEvents.remove(node);
        }
    }

    protected void addRowEvent(ITableEvent tableEvent, IRowEvent.Type eventType, int row, IRowFlags rowFlags) {
        IRowEvent rowBuilder = createRowEvent(eventType, row);
        if (!eventType.equals(IRowEvent.Type.Remove)) {
            List<ColumnHolder> columnHolders = owner.getInput(Constants.IN).getProducer().getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                if (rowFlags != null && !rowFlags.isDirty(columnHolder.getColumnId())) {
                    continue;
                }
                IRowEvent.IColumnValue valueBuilder = MessagePool.getInstance().get(IRowEvent.IColumnValue.class)
                        .setColumnId(columnHolder.getColumnId());
                switch (columnHolder.getType()) {
                    case Bool: {
                        valueBuilder.setBooleanValue(((IColumnBool) columnHolder).getBool(row));
                        break;
                    }
                    case NullableBool: {
                        NullableBool value = ((IColumnNullableBool) columnHolder).getNullableBool(row);
                        if (value == NullableBool.Null) {
                            valueBuilder.setNullValue();
                        } else {
                            valueBuilder.setBooleanValue(value == NullableBool.True);
                        }
                        break;
                    }
                    case Byte: {
                        valueBuilder.setIntegerValue(((IColumnByte) columnHolder).getByte(row));
                        break;
                    }
                    case Short: {
                        valueBuilder.setIntegerValue(((IColumnShort) columnHolder).getShort(row));
                        break;
                    }
                    case Int: {
                        valueBuilder.setIntegerValue(((IColumnInt) columnHolder).getInt(row));
                        break;
                    }
                    case Long: {
                        valueBuilder.setLongValue(((IColumnLong) columnHolder).getLong(row));
                        break;
                    }
                    case Float: {
                        valueBuilder.setFloatValue(((IColumnFloat) columnHolder).getFloat(row));
                        break;
                    }
                    case Double: {
                        valueBuilder.setDoubleValue(((IColumnDouble) columnHolder).getDouble(row));
                        break;
                    }
                    case String: {
                        String value = ((IColumnString) columnHolder).getString(row);

                        if(value != null) {
                            valueBuilder.setStringValue(value);
                        }else{
                            valueBuilder.setNullValue();
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Cannot serialise type " + columnHolder.getType());
                    }
                }
                rowBuilder.getColumnValues().add(valueBuilder);
                valueBuilder.release();
            }
        }
        tableEvent.getRowEvents().add(rowBuilder);
        rowBuilder.release();
    }

    private IRowEvent createRowEvent(IRowEvent.Type eventType, int row) {
        return MessagePool.getInstance().get(IRowEvent.class)
                .setType(eventType)
                .setRowId(row);
    }
}
