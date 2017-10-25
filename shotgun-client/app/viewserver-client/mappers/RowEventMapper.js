import ProtoLoader from '../core/ProtoLoader';
import RowEvent from '../domain/RowEvent';
import {$} from '../core/JQueryish';

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
      rowId: (rowEvent.rowId >= 0 ? rowEvent.rowId : undefined)
    });

    const columnValueDtos = [];
    console.log(`Column values ${JSON.stringify(rowEvent.columnValues)}`);
    $.each(rowEvent.columnValues, (key, value) => {
      let columnId;
      console.log('Key is ' + key);
      if (typeof key === 'string') {
        columnId = parseInt(key, 10);
        if (isNaN(columnId)) {
          console.log('Getting col id from data sink');
          columnId = dataSink.getColumnId(key);
        }
      } else {
        columnId = key;
      }
      const columnValue = ProtoLoader.Dto.RowEventDto.ColumnValue.create({columnId});
      if (value === null) {
        columnValue.nullValue = -1; // -1 is arbitrary
      } else {
        const column = dataSink.getColumn(columnId);
        console.log(`!!! Row mapper found column "${JSON.stringify(column)}"`);
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