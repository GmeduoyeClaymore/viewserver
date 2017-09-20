import ProtoLoader from '../core/ProtoLoader';
import {$} from '../core/JQueryish';

export default class OptionsMapper{
    toDto(options) {
        var _self = this;
        var optionsCopy = $.extend(true, {}, options);

        if(optionsCopy.columnsToSort !== undefined){
            $.each(optionsCopy.columnsToSort, function(index, columnToSort){
                columnToSort.name = _self._parseSortColumn(columnToSort.name);
                columnToSort.direction = _self._parseDirection(columnToSort.direction);
            });
        }

        return new ProtoLoader.Dto.OptionsDto(optionsCopy);
    }

    _parseSortColumn(sortColumn){
        return sortColumn.replace(/[\[\]]/g, '').replace(/\./g, '_');
    }

    _parseDirection (direction) {
        switch (direction.toLowerCase()) {
            case 'asc': {
                return ProtoLoader.Dto.SortDirection.ASCENDING;
            }
            case 'desc': {
                return ProtoLoader.Dto.SortDirection.DESCENDING;
            }
            default: {
                throw new Error('Inew Error(nvalid sort direction ' + direction);
            }
        }
    }
}