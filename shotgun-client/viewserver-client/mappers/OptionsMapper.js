import ProtoLoader from '../core/ProtoLoader';
import {$} from '../core/JQueryish';

export default class OptionsMapper{
    static toDto(options) {
        var _self = OptionsMapper;
        var optionsCopy = $.extend(true, {}, options);

        if(optionsCopy.columnsToSort !== undefined){
            $.each(optionsCopy.columnsToSort, function(index, columnToSort){
                columnToSort.name = _self._parseSortColumn(columnToSort.name);
                columnToSort.direction = _self._parseDirection(columnToSort.direction);
            });
        }

        return ProtoLoader.Dto.OptionsDto.create(optionsCopy);
    }

    static _parseSortColumn(sortColumn){
        return sortColumn.replace(/[\[\]]/g, '').replace(/\./g, '_');
    }

    static _parseDirection (direction) {
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