import Logger from '../Logger';

export default class RowMapper{
  static _parseValue(rowValue) {
    const value = rowValue[rowValue.value];
    try {
      switch (rowValue.value) {
        case 'longValue':
          return value;
        case 'nullValue':
          return undefined;
        default:
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
    rowValues.forEach(rowValue => {
      const columnName = schema[rowValue.columnId].name;
      const columnValue = _self._parseValue(rowValue);
      row[columnName] = columnValue;
    });

    return row;
  }
}
