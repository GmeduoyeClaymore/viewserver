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

import io.viewserver.messages.IMessage;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.command.*;
import io.viewserver.messages.config.*;
import io.viewserver.messages.heartbeat.IHeartbeat;
import io.viewserver.messages.protobuf.dto.MessageMessage;
import io.viewserver.messages.tableevent.*;

/**
 * Created by nick on 02/12/15.
 */
public class Message extends PoolableMessage<Message> implements IMessage<Message> {
    private MessageMessage.MessageDto messageDto;
    private Heartbeat heartbeat;
    private Command command;
    private CommandResult commandResult;
    private TableEvent tableEvent;

    static {
        MessagePool.getInstance().createPool(IMessage.class, Message::new);
        MessagePool.getInstance().createPool(IHeartbeat.class, Heartbeat::new);
        MessagePool.getInstance().createPool(ICommand.class, Command::new);
        MessagePool.getInstance().createPool(ICommandResult.class, CommandResult::new);

        MessagePool.getInstance().createPool(IAuthenticateCommand.class, AuthenticateCommand::new);
        MessagePool.getInstance().createPool(IConfigurateCommand.class, ConfigurateCommand::new);
        MessagePool.getInstance().createPool(ISubscribeCommand.class, SubscribeCommand::new);
        MessagePool.getInstance().createPool(ISubscribeDataSourceCommand.class, SubscribeDataSourceCommand::new);
        MessagePool.getInstance().createPool(ISubscribeReportCommand.class, SubscribeReportCommand::new);
        MessagePool.getInstance().createPool(ISubscribeDimensionCommand.class, SubscribeDimensionCommand::new);
        MessagePool.getInstance().createPool(IUpdateSubscriptionCommand.class, UpdateSubscriptionCommand::new);
        MessagePool.getInstance().createPool(IUnsubscribeCommand.class, UnsubscribeCommand::new);
        MessagePool.getInstance().createPool(IRegisterSlaveCommand.class, RegisterSlaveCommand::new);
        MessagePool.getInstance().createPool(IInitialiseSlaveCommand.class, InitialiseSlaveCommand::new);
        MessagePool.getInstance().createPool(IRegisterDataSourceCommand.class, RegisterDataSourceCommand::new);
        MessagePool.getInstance().createPool(IUpdateDimensionMapCommand.class, UpdateDimensionMapCommand::new);

        MessagePool.getInstance().createPool(IReportContext.class, ReportContext::new);
        MessagePool.getInstance().createPool(IReportContext.IDimensionValue.class, ReportContext.DimensionValue::new);
        MessagePool.getInstance().createPool(IReportContext.IParameterValue.class, ReportContext.ParameterValue::new);
        MessagePool.getInstance().createPool(IReportContext.IValue.class, ReportContext.Value::new);

        MessagePool.getInstance().createPool(IOptions.class, Options::new);
        MessagePool.getInstance().createPool(IOptions.ISortColumn.class, Options.SortColumn::new);

        MessagePool.getInstance().createPool(ITableEvent.class, TableEvent::new);
        MessagePool.getInstance().createPool(ISchemaChange.class, SchemaChange::new);
        MessagePool.getInstance().createPool(ISchemaChange.IAddColumn.class, SchemaChange.AddColumn::new);
        MessagePool.getInstance().createPool(ISchemaChange.IRemoveColumn.class, SchemaChange.RemoveColumn::new);
        MessagePool.getInstance().createPool(IRowEvent.class, RowEvent::new);
        MessagePool.getInstance().createPool(IRowEvent.IColumnValue.class, RowEvent.ColumnValue::new);
        MessagePool.getInstance().createPool(IStatus.class, Status::new);
        MessagePool.getInstance().createPool(ITableMetadata.class, TableMetadata::new);
        MessagePool.getInstance().createPool(ITableMetadata.IMetadataValue.class, TableMetadata.MetadataValue::new);
        MessagePool.getInstance().createPool(ITableMetadata.IValue.class, TableMetadata.Value::new);

        MessagePool.getInstance().createPool(ITableEditCommand.class, TableEditCommand::new);
        MessagePool.getInstance().createPool(ITableEditCommand.ICreationConfig.class, TableEditCommand.CreationConfig::new);

        MessagePool.getInstance().createPool(IUpdateDimensionMapCommand.IDataSource.class, UpdateDimensionMapCommand.DataSource::new);
        MessagePool.getInstance().createPool(IUpdateDimensionMapCommand.IDimension.class, UpdateDimensionMapCommand.Dimension::new);
        MessagePool.getInstance().createPool(IUpdateDimensionMapCommand.IMapping.class, UpdateDimensionMapCommand.Mapping::new);

        MessagePool.getInstance().createPool(IConfigurateCommand.class, ConfigurateCommand::new);
        MessagePool.getInstance().createPool(IConfigurateCommand.IOperator.class, ConfigurateCommand.Operator::new);
        MessagePool.getInstance().createPool(IConfigurateCommand.IConnection.class, ConfigurateCommand.Connection::new);
        MessagePool.getInstance().createPool(IConfigurateCommand.IMetadataItem.class, ConfigurateCommand.MetadataItem::new);
        MessagePool.getInstance().createPool(IFilterConfig.class, FilterConfig::new);
        MessagePool.getInstance().createPool(IProjectionConfig.class, ProjectionConfig::new);
        MessagePool.getInstance().createPool(IProjectionConfig.IProjectionColumn.class, ProjectionConfig.ProjectionColumn::new);
        MessagePool.getInstance().createPool(IGroupByConfig.class, GroupByConfig::new);
        MessagePool.getInstance().createPool(IGroupByConfig.ISummary.class, GroupByConfig.Summary::new);
        MessagePool.getInstance().createPool(ICalcColConfig.class, CalcColConfig::new);
        MessagePool.getInstance().createPool(ICalcColConfig.ICalculatedColumn.class, CalcColConfig.CalculatedColumn::new);
        MessagePool.getInstance().createPool(ICalcColConfig.ICalculationAlias.class, CalcColConfig.CalculationAlias::new);
        MessagePool.getInstance().createPool(ISortConfig.class, SortConfig::new);
        MessagePool.getInstance().createPool(ISortConfig.ISortDescriptor.class, SortConfig.SortDescriptor::new);
        MessagePool.getInstance().createPool(ISortConfig.ISortColumn.class, SortConfig.SortColumn::new);
        MessagePool.getInstance().createPool(ITransposeConfig.class, TransposeConfig::new);
        MessagePool.getInstance().createPool(IRollingTableConfig.class, RollingTableConfig::new);
        MessagePool.getInstance().createPool(IIndexConfig.class, IndexConfig::new);
        MessagePool.getInstance().createPool(IIndexConfig.IOutput.class, IndexConfig.Output::new);
        MessagePool.getInstance().createPool(IIndexConfig.IQueryHolder.class, IndexConfig.QueryHolder::new);
    }

    public Message() {
        super(IMessage.class);
    }

    @Override
    public Type getType() {
        final MessageMessage.MessageDto.MessageCase messageCase = messageDto.getMessageCase();
        switch (messageCase) {
            case HEARTBEAT: {
                return Type.Heartbeat;
            }
            case COMMAND: {
                return Type.Command;
            }
            case COMMANDRESULT: {
                return Type.CommandResult;
            }
            case TABLEEVENT: {
                return Type.TableEvent;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown message type '%s' in DTO", messageCase));
            }
        }
    }

    @Override
    public IHeartbeat getHeartbeat() {
        if (heartbeat == null) {
            heartbeat = messageDto != null ? Heartbeat.fromDto(messageDto.getHeartbeat())
                    : (Heartbeat)MessagePool.getInstance().get(IHeartbeat.class);
        }
        return heartbeat;
    }

    @Override
    public IMessage setHeartbeat(IHeartbeat heartbeat) {
        if (this.heartbeat != null) {
            if (messageDto == null) {
                this.heartbeat.release();
            }
        }
        this.heartbeat = (Heartbeat) heartbeat.retain();
        return this;
    }

    @Override
    public ICommand getCommand() {
        if (command == null) {
            command = messageDto != null ? Command.fromDto(messageDto.getCommand())
                    : (Command)MessagePool.getInstance().get(ICommand.class);
        }
        return command;
    }

    @Override
    public IMessage setCommand(ICommand command) {
        if (this.command != null) {
            if (messageDto == null) {
                this.command.release();
            }
        }
        this.command = (Command)command.retain();
        return this;
    }

    @Override
    public ICommandResult getCommandResult() {
        if (commandResult == null) {
            commandResult = messageDto != null ? CommandResult.fromDto(messageDto.getCommandResult())
                    : (CommandResult)MessagePool.getInstance().get(ICommandResult.class);
        }
        return commandResult;
    }

    @Override
    public IMessage setCommandResult(ICommandResult commandResult) {
        if (this.commandResult != null) {
            if (messageDto == null) {
                this.commandResult.release();
            }
        }
        this.commandResult = (CommandResult)commandResult.retain();
        return this;
    }

    @Override
    public ITableEvent getTableEvent() {
        if (tableEvent == null) {
            tableEvent = messageDto != null ? TableEvent.fromDto(messageDto.getTableEvent())
                    : (TableEvent)MessagePool.getInstance().get(ITableEvent.class);
        }
        return tableEvent;
    }

    @Override
    public IMessage setTableEvent(ITableEvent tableEvent) {
        if (this.tableEvent != null) {
            if (messageDto == null) {
                this.tableEvent.release();
            }
        }
        this.tableEvent = (TableEvent)tableEvent.retain();
        return this;
    }

    MessageMessage.MessageDto.Builder getBuilder() {
        final MessageMessage.MessageDto.Builder builder = MessageMessage.MessageDto.newBuilder();
        if (heartbeat != null) {
            builder.setHeartbeat(heartbeat.getBuilder());
        } else if (command != null) {
            builder.setCommand(command.getBuilder());
        } else if (commandResult != null) {
            builder.setCommandResult(commandResult.getBuilder());
        } else if (tableEvent != null) {
            builder.setTableEvent(tableEvent.getBuilder());
        }
        return builder;
    }

    @Override
    protected void doRelease() {
        if (heartbeat != null) {
            heartbeat.release();
            heartbeat = null;
        }
        if (command != null) {
            command.release();
            command = null;
        }
        if (commandResult != null) {
            commandResult.release();
            commandResult = null;
        }
        if (tableEvent != null) {
            tableEvent.release();
            tableEvent = null;
        }
        messageDto = null;
    }

    public static Message fromDto(MessageMessage.MessageDto messageDto) {
        final Message message = (Message)MessagePool.getInstance().get(IMessage.class);
        message.messageDto = messageDto;
        return message;
    }
}
