import ProtoLoader from '../core/ProtoLoader';

export default class OptionsMapper{
  static toDto(options) {
    const _self = OptionsMapper;
    let optionsCopy = JSON.parse(JSON.stringify(options));
    const {offset, limit, columnName, columnsToSort, filterMode, flags} = optionsCopy;
    optionsCopy = {offset, limit, columnName, columnsToSort, filterMode, flags};

    if (optionsCopy.columnsToSort !== undefined){
      optionsCopy.columnsToSort.forEach(columnToSort => {
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
        return ProtoLoader.Dto.SortDirection.values.ASCENDING;
      }
      case 'desc': {
        return ProtoLoader.Dto.SortDirection.values.DESCENDING;
      }
      default: {
        throw new Error('Inew Error(nvalid sort direction ' + direction);
      }
    }
  }
}
