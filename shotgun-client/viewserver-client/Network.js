import ProtoLoader from './core/ProtoLoader';
import {$} from './core/JQueryish';
import Logger from './Logger';
import RowMapper from './mappers/RowMapper';
import Connection from './Connection';
import TableMetaDataMapper from './mappers/TableMetaDataMapper';

export default class Network {
    constructor() {
    }

    connect(){
        this.connection = new Connection("ws://localhost:8080");
    }
    
    get socket(){
        if(!this.connection){
            throw new Error("Connect must be called before attempting to retrieve socket")
        }
        return this.connection.socket;
    }
    
    get openCommands(){
        if(!this.connection){
            throw new Error("Connect must be called before attempting to retrieve socket")
        }
        return this.connection.openCommands;
    }

    //Private functions
    send(buffer) {
        if (this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(buffer);
        } else {
            Logger.error('Web socket is not open! (state=' + this.socket.readyState + ')');
        }
    }

    sendMessage(message) {
        var messageWrapper = new ProtoLoader.Dto.MessageDto();
        if (message instanceof ProtoLoader.Dto.HeartbeatDto) {
            messageWrapper.heartbeat = message;
        } else if (message instanceof ProtoLoader.Dto.CommandDto) {
            messageWrapper.command = message;
        }
        this.send(messageWrapper.toArrayBuffer());
    }


    sendHeartbeat() {
        var heartbeat = new ProtoLoader.Dto.HeartbeatDto(ProtoLoader.Dto.HeartbeatDto.Type.PING);
        this.sendMessage(heartbeat);
    }

    receiveHeartBeat(heartBeat) {
        if (heartBeat.type === ProtoLoader.Dto.HeartbeatDto.Type.PING) {
            this.sendHeartbeat();
        } else {
            // Logger.debug('PONG!');
        }
    }

    receiveCommandResult(commandResult) {
        Logger.debug('Received command result', commandResult);

        let command = this.openCommands[commandResult.getId()];

        if (!command) {
            Logger.warning('Received command result from command that no longer exists. Was this command cancelled ??' + commandResult.getId());
            return;
        }

        if (!commandResult.getSuccess() || !command.continuous) {
            delete this.openCommands[this.openCommands.indexOf(command)];
        }

        if (command.handler) {
            if (commandResult.getSuccess()) {
                command.handler.onSuccess(commandResult.getId());
            } else {
                command.handler.onError(commandResult.getMessage());
            }
        }
    }

    receiveTableEvent(tableEvent) {
        Logger.fine('Received table event', tableEvent);

        let command = this.openCommands[tableEvent.getId()];

        if (!command) {
            Logger.warning('Could not find command: ' + tableEvent.getId() + ' subscription has most likely been cancelled while data on the wire');
            return;
        }

        let tableMetaData = TableMetaDataMapper.fromDto(tableEvent.metaData);

        if (command.handler) {
            command.handler.onTotalRowCount(tableMetaData.totalSize);

            this.handleStatuses(tableEvent.statuses, command.handler);
            this.handleSchemaChange(tableEvent.schemaChange, command.handler);
            this.handleRowEvents(tableEvent.rowEvents, command.handler);
            this.handleFlags(tableEvent, command.handler);
        }
    }

    handleStatuses(statuses, handler) {
        if (statuses) {
            $.each(statuses, function (index, statusDto) {
                switch (statusDto.status) {
                    case ProtoLoader.Dto.StatusDto.StatusId.SCHEMARESET:
                    {
                        Logger.fine('Received schema reset');
                        handler.onSchemaReset();
                        break;
                    }
                    case ProtoLoader.Dto.StatusDto.StatusId.DATARESET:
                    {
                        Logger.fine('Received data reset');
                        handler.onDataReset();
                        break;
                    }
                    default:
                    {
                        Logger.warning('Received unknown status ' + statusDto.status);
                        break;
                    }
                }
            });
        }
    }

    handleFlags(tableEvent, handler) {
        if (tableEvent.flags && tableEvent.flags === 1) {
            handler.onSnapshotComplete();
        }
    }

    handleSchemaChange(schemaChange, handler) {
        if (schemaChange) {
            $.each(schemaChange.addedColumns, function (index, addedColumn) {
                Logger.fine('Column added', addedColumn);

                handler.onColumnAdded(addedColumn.columnId, {name: addedColumn.name, type: addedColumn.type});
            });

            $.each(schemaChange.removedColumns, function (index, removedColumnId) {
                Logger.fine('Column removed', removedColumnId);
                handler.onColumnRemoved(removedColumnId);
            });
        }
    }

    handleRowEvents(rowEvents, handler) {
        if (rowEvents) {
            let row;

            $.each(rowEvents, function (index, rowEvent) {
                switch (rowEvent.eventType) {
                    case 0: //add
                        row = RowMapper.fromDto(handler.schema, rowEvent.values);
                        Logger.fine('Row added', row);
                        handler.onRowAdded(rowEvent.rowId, row);
                        break;
                    case 1: //update
                        row = RowMapper.fromDto(handler.schema, rowEvent.values);
                        Logger.fine('Row updated', row);
                        handler.onRowUpdated(rowEvent.rowId, row);
                        break;
                    case 2: //remove
                        handler.onRowRemoved(rowEvent.rowId);
                        break;
                    default:
                        Logger.error('Unknown row event type received: ' + rowEvent.eventType);
                }
            });
        }
    }

}