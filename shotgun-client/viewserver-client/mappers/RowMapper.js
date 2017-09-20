import {$} from '../core/JQueryish';

export default class RowMapper{
    _parseValue(rowValue) {
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

    fromDto(schema, rowValues) {
        var _self = this;
        var row = {};

        $.each(rowValues, function (index, rowValue) {
            var columnName = schema[rowValue.columnId].name;
            var columnValue = _self._parseValue(rowValue);

            row[columnName] = columnValue;
        });

        return row;
    }
}