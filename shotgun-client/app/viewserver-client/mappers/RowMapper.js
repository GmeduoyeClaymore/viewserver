import Logger from 'common/Logger';
import ProtoLoader from '../core/ProtoLoader';

export default class RowMapper{
  static _parseValue(rowValue, column) {
    const value = rowValue[rowValue.value];

    try {
      switch (rowValue.value) {
      case 'longValue':
        return value;
      case 'nullValue':
        return undefined;
      default:
        if (column.dataType === ProtoLoader.Dto.DataType.JSON && value) {
          return JSON.parse(value);
        }
        return value;
      }
    } catch (error){
      Logger.error('error parsing value ' + JSON.stringify(rowValue));
      throw error;
    }
  }

  static fromDto(schema, rowValues) {
    const _self = RowMapper;
    const row = {};
    Logger.fine('Mapping row values ' + JSON.stringify(schema));
    console.log('Mapping row values ' + JSON.stringify(schema));
    rowValues.forEach(rowValue => {
      const column = schema[rowValue.columnId];
      const columnName = column.name;
      const columnValue = _self._parseValue(rowValue, column);
      row[columnName] = columnValue;
    });

    return row;
  }
}
