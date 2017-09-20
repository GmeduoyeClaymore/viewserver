import {$} from '../core/JQueryish';

export default class TableMetaDataMapper{
    fromDto(tableMetaDataDto) {
        var metaData = {};

        $.each(tableMetaDataDto.metaDataValue, function(index, metaDataValue){
            metaData[metaDataValue.name] = metaDataValue.value[metaDataValue.value.value];
        });
        return metaData;
    }
}