import ProtoLoader from '../core/ProtoLoader';
import RowEvent from '../domain/RowEvent';
import {$} from '../core/JQueryish';

export default class RowEventMapper{
    
    mapRowEventType(rowEventType) {
        switch (rowEventType) {
            case RowEvent.EventType.ADD: {
                return ProtoLoader.Dto.RowEventDto.RowEventType.ADD;
            }
            case RowEvent.EventType.UPDATE: {
                return ProtoLoader.Dto.RowEventDto.RowEventType.UPDATE;
            }
            case RowEvent.EventType.REMOVE: {
                return ProtoLoader.Dto.RowEventDto.RowEventType.REMOVE;
            }
            default : {
                throw new Error()
            }
        }
    }

    toDto(rowEvent, dataSink) {
        let rowEventDto = new ProtoLoader.Dto.RowEventDto(this.mapRowEventType(rowEvent.type));
        if (rowEvent.rowId >= 0) {
            rowEventDto.setRowId(rowEvent.rowId);
        }
        let columnValueDtos = [];
        $.each(rowEvent.columnValues, function (key, value) {
            let columnId;
            if (typeof key === 'string') {
                columnId = parseInt(key,10);
                if (isNaN(columnId)) {
                    columnId = dataSink.getColumnId(key);
                }
            } else {
                columnId = key;
            }
            let columnValue = new ProtoLoader.Dto.RowEventDto.ColumnValue(columnId);
            if (value === null) {
                columnValue.set('nullValue', -1); // -1 is arbitrary
            } else {
                let column = dataSink.getColumn(columnId);
                switch (column.type) {
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.BOOLEAN:
                    {
                        columnValue.set('booleanValue', value);
                        break;
                    }
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.BYTE:
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.SHORT:
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.INTEGER:
                    {
                        columnValue.set('intValue', value);
                        break;
                    }
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.LONG:
                    {
                        columnValue.set('longValue', value);
                        break;
                    }
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.FLOAT:
                    {
                        columnValue.set('floatValue', value);
                        break;
                    }
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.DOUBLE:
                    {
                        columnValue.set('doubleValue', value);
                        break;
                    }
                    case ProtoLoader.Dto.SchemaChangeDto.AddColumn.ColumnType.STRING:
                    {
                        columnValue.set('stringValue', value);
                        break;
                    }
                    default : {
                        throw new Error("Unable to map column type " + column.type)
                    }
                }
            }
            columnValueDtos.push(columnValue);
        });
        rowEventDto.setValues(columnValueDtos);
        return rowEventDto;
    }
}