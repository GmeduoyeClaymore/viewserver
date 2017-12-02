import ProtoLoader from '../core/ProtoLoader';
import RowEvent from '../domain/RowEvent';
import Logger from 'common/Logger';

export default class RowEventMapper{
  static mapRowEventType(rowEventType) {
    switch (rowEventType) {
    case RowEvent.EventType.ADD: {
      return 0;
    }
    case RowEvent.EventType.UPDATE: {
      return 1;
    }
    case RowEvent.EventType.REMOVE: {
      return 2;
    }
    default : {
      throw new Error();
    }
    }
  }

  static toDto(rowEvent, dataSink) {
    const rowEventDto = ProtoLoader.Dto.RowEventDto.create({
      eventType: RowEventMapper.mapRowEventType(rowEvent.type),
      tableKey: rowEvent.tableKey,
      rowId: (rowEvent.rowId >= 0 ? rowEvent.rowId : undefined)
    });

    const columnValueDtos = [];
    Logger.debug(`Column values ${JSON.stringify(rowEvent.columnValues)}`);
    Object.entries(rowEvent.columnValues).forEach(([key, value]) => {
      let columnId;
      Logger.debug('Key is ' + key);
      if (typeof key === 'string') {
        columnId = parseInt(key, 10);
        if (isNaN(columnId)) {
          Logger.debug('Getting col id from data sink');
          columnId = dataSink.getColumnId(key);
        }
      } else {
        columnId = key;
      }
      if (typeof columnId === 'undefined' ){
        Logger.warning(`Unable to find column ${key} on schemal ${JSON.stringify(dataSink.schema)}`);
        return;
      }
      const columnValue = ProtoLoader.Dto.RowEventDto.ColumnValue.create({columnId});
      if (value === null) {
        columnValue.nullValue = -1; // -1 is arbitrary
      } else {
        const column = dataSink.getColumn(columnId);
        Logger.debug(`!!! Row mapper found column "${JSON.stringify(column)}"`);
        if (!column){
          throw new Error(`Unable to find column \"${key}\" id \"${columnId}\" on schemal ${JSON.stringify(dataSink.schema)}`);
        }
        switch (column.type) {
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.BOOLEAN:
        {
          columnValue.booleanValue  = value;
          break;
        }
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.BYTE:
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.SHORT:
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.INTEGER:
        {
          columnValue.intValue = value;
          break;
        }
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.LONG:
        {
          columnValue.longValue = value;
          break;
        }
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.FLOAT:
        {
          columnValue.floatValue = value;
          break;
        }
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.DOUBLE:
        {
          columnValue.doubleValue = value;
          break;
        }
        case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.STRING:
        {
          columnValue.stringValue = value;
          break;
        }
        default : {
          throw new Error('Unable to map column type ' + column.type);
        }
        }
      }
      columnValueDtos.push(columnValue);
    });
    rowEventDto.values = columnValueDtos;
    return rowEventDto;
  }
}
