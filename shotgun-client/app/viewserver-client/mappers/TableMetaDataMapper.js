import Logger from 'common/Logger';

export default class TableMetaDataMapper{
  static fromDto(tableMetaDataDto) {
    const metaData = {};

    tableMetaDataDto.metaDataValue.forEach(metaDataValue => {
      Logger.debug('Mapping' + JSON.stringify(metaDataValue));
      metaData[metaDataValue.name] = metaDataValue.value[metaDataValue.value.value];
    });
    return metaData;
  }
}
