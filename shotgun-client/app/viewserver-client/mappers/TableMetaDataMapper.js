import {$} from '../core/JQueryish';

export default class TableMetaDataMapper{
  static fromDto(tableMetaDataDto) {
    const metaData = {};

    $.each(tableMetaDataDto.metaDataValue, (index, metaDataValue) => {
      console.log('Mapping' + JSON.stringify(metaDataValue));
      metaData[metaDataValue.name] = metaDataValue.value[metaDataValue.value.value];
    });
    return metaData;
  }
}
