import {$} from '../core/JQueryish';

export default class RowMapper{
    static _parseValue(rowValue) {
        var value = rowValue[rowValue.value];

        switch (rowValue.value) {
            case 'longValue':
                return value.toNumber();
            case 'nullValue':
                return undefined;
            default:
                return value;

        }
    }

    static fromDto(schema, rowValues) {
        var _self = RowMapper;
        var row = {};

        $.each(rowValues, function (index, rowValue) {
            console.log("Mapping row value " + JSON.stringify(schema));
            var columnName = schema[rowValue.columnId].name;
            var columnValue = _self._parseValue(rowValue);

            row[columnName] = columnValue;
        });

        return row;
    }
}