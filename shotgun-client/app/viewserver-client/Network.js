import ProtoLoader from './core/ProtoLoader';
import Logger from 'common/Logger';
import RowMapper from './mappers/RowMapper';
import Connection from './Connection';
import TableMetaDataMapper from './mappers/TableMetaDataMapper';

export default class Network {
  constructor(connectionUrl) {
    this.connectionUrl = connectionUrl;
    Logger.debug(`Creating connection to ${this.connectionUrl}`);
    this.connection = new Connection(this.connectionUrl, this);
  }

  connect(autoReconnect){
    Logger.debug(`Connecting to ${this.connectionUrl} auto reconnect is ${autoReconnect}`);
    return this.connection.connect(autoReconnect);
  }

  get connected(){
    return this.connection.connected;
  }
    
  get socket(){
    if (!this.connection){
      throw new Error('Connect must be called before attempting to retrieve socket');
    }
    return this.connection.socket;
  }
    
  get openCommands(){
    if (!this.connection){
      throw new Error('Connect must be called before attempting to retrieve socket');
    }
    return this.connection.openCommands;
  }

  //Private functions
  send(buffer) {
    if (this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(buffer);
    } else {
      Logger.info('Web socket is not open! (state=' + this.socket.readyState + ')');
    }
  }

  sendCommand(command) {
    return this.connection.sendCommand(command);
  }

  removeOpenCommand(commandId) {
    return this.connection.removeOpenCommand(commandId);
  }

  sendMessage(message) {
    let messageWrapper = null;
        
    if (message.command) {
      messageWrapper = ProtoLoader.Dto.MessageDto.create({
        command: message
      });
    } else {
      messageWrapper = ProtoLoader.Dto.MessageDto.create({
        heartbeat: message
      });
    }
    const result = ProtoLoader.Dto.MessageDto.encode(messageWrapper).finish();
    this.send(result);
  }


  sendHeartbeat() {
    const heartbeat = ProtoLoader.Dto.HeartbeatDto.create({Type: 2});
    this.sendMessage(heartbeat);
  }

  receiveHeartBeat(heartBeat) {
    if (heartBeat.type === 1) {
      this.sendHeartbeat();
    } else {
      // Logger.debug('PONG!');
    }
  }

  receiveCommandResult(commandResult) {
    Logger.fine('Received command result' + JSON.stringify(commandResult));

    const command = this.openCommands[commandResult.id];

    if (!command) {
      Logger.warning('Received command result from command that no longer exists. Was this command cancelled ??' + commandResult.id);
      return;
    }

    if (!commandResult.success || !command.continuous) {
      delete this.openCommands[this.openCommands.indexOf(command)];
    }

    if (command.handler) {
      if (commandResult.success) {
        if (command.handler.onSuccess){
          command.handler.onSuccess(commandResult.id, commandResult.message);
        }
      } else {
        if (command.handler.onError){
          command.handler.onError(commandResult.message);
        } else {
          Logger.error(commandResult.message);
        }
      }
    }
  }

  receiveTableEvent(tableEvent) {
    Logger.fine('Received table event', tableEvent);

    const command = this.openCommands[tableEvent.id];

    if (!command) {
      Logger.warning('Could not find command: ' + tableEvent.id + ' subscription has most likely been cancelled while data on the wire');
      return;
    }

    const tableMetaData = TableMetaDataMapper.fromDto(tableEvent.metaData);

    if (command.handler) {
      if (command.handler.onTotalRowCount){
        command.handler.onTotalRowCount(tableMetaData.totalSize);
      } else {
        Logger.warning('couldnt find total row count handler');
      }

      this.handleStatuses(tableEvent.statuses, command.handler);
      this.handleSchemaChange(tableEvent.schemaChange, command.handler);
      this.handleRowEvents(tableEvent.rowEvents, command.handler);
      this.handleFlags(tableEvent, command.handler);
    }
  }

  //DataReset(1),
  //SchemaReset(2),
  //SchemaError(3),
  //SchemaErrorCleared(4),
  //ConfigError(5),
  //ConfigErrorCleared(6),
  //DataError(7),
  //DataErrorCleared(8);

  handleStatuses(statuses, handler) {
    if (statuses) {
      statuses.forEach((statusDto) => {
        switch (statusDto.status) {
        case 0:
        {
          Logger.debug('Received data reset');
          if (handler.onDataReset){
            handler.onDataReset();
          }
          break;
        }
        case 1:
        {
          Logger.debug('Received schema reset');
          if (handler.onSchemaReset){
            handler.onSchemaReset();
          }
          break;
        }
        case 2:
        {
          Logger.fine('Received schema error');
          if (handler.onSchemaError){
            handler.onSchemaError(statusDto.error);
          }
          break;
        }
        case 3:
        {
          Logger.fine('Received schema error cleared');
          if (handler.onSchemaErrorCleared){
            handler.onSchemaErrorCleared();
          }
          break;
        }
        case 4:
        {
          Logger.fine('Received Config error');
          if (handler.onConfigError){
            handler.onConfigError(statusDto.error);
          }
          break;
        }
        case 5:
        {
          Logger.fine('Received Config error cleared');
          if (handler.onConfigErrorCleared){
            handler.onConfigErrorCleared();
          }
          break;
        }
        case 6:
        {
          Logger.fine('Received Data error');
          if (handler.onDataError){
            handler.onDataError(statusDto.error);
          }
          break;
        }
        case 7:
        {
          Logger.fine('Received Data error cleared');
          if (handler.onDataErrorCleared){
            handler.onDataErrorCleared();
          }
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
      schemaChange.addedColumns.forEach(addedColumn => {
        Logger.fine('Column added', addedColumn);
        const {type, dataType, name} = addedColumn;
        handler.onColumnAdded(addedColumn.columnId, {type, dataType, name});
      });

      schemaChange.removedColumns.forEach(removedColumnId => {
        Logger.fine('Column removed', removedColumnId);
        handler.onColumnRemoved(removedColumnId);
      });
    }
  }

  handleRowEvents(rowEvents, handler) {
    if (rowEvents) {
      let row;

      rowEvents.forEach(rowEvent => {
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
