import {$} from '../core/JQueryish';

export default class RowMapper{
    static _parseValue(rowValue) {
        var value = rowValue[rowValue.value];
        try{
            switch (rowValue.value) {
                case 'longValue':
                    return value.toNumber ? value.toNumber() : parseFloat(value.toNumber);
                case 'nullValue':
                    return undefined;
                default:
                    return value;

            }
        }catch(error){
            console.error("error parsing value " + JSON.stringify(rowValue));
            throw error;
        }
    }

    static fromDto(schema, rowValues) {
        var _self = RowMapper;
        var row = {};
        console.log("Mapping row values " + JSON.stringify(schema));
        $.each(rowValues, function (index, rowValue) {
            var columnName = schema[rowValue.columnId].name;
            var columnValue = _self._parseValue(rowValue);
            row[columnName] = columnValue;
        });

        return row;
    }
}