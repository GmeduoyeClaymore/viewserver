import {$} from '../core/JQueryish';

export default class RowMapper{
  static _parseValue(rowValue) {
    const value = rowValue[rowValue.value];
    try {
      switch (rowValue.value) {
        case 'longValue':
          return value.toNumber ? value.toNumber() : parseFloat(value.toNumber);
        case 'nullValue':
          return undefined;
        default:
          return value;
      }
    } catch (error){
      console.error('error parsing value ' + JSON.stringify(rowValue));
      throw error;
    }
  }

  static fromDto(schema, rowValues) {
    const _self = RowMapper;
    const row = {};
    console.log('Mapping row values ' + JSON.stringify(schema));
    $.each(rowValues, (index, rowValue) => {
      const columnName = schema[rowValue.columnId].name;
      const columnValue = _self._parseValue(rowValue);
      row[columnName] = columnValue;
    });

    return row;
  }
}
