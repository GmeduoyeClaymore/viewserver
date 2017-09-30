import {$} from '../core/JQueryish';

export default class TableMetaDataMapper{
    static fromDto(tableMetaDataDto) {
        var metaData = {};

        $.each(tableMetaDataDto.metaDataValue, function(index, metaDataValue){
			console.log("Mapping" + JSON.stringify(metaDataValue));
            metaData[metaDataValue.name] = metaDataValue.value[metaDataValue.value.value];
        });
        return metaData;
    }
}