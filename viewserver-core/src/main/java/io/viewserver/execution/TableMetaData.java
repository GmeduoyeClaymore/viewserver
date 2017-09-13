/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.execution;

import io.viewserver.messages.MessagePool;
import io.viewserver.messages.tableevent.ITableMetadata;

import java.util.HashMap;
import java.util.List;

public class TableMetaData extends HashMap<String, Object> {
    public ITableMetadata toMessage() {
        final ITableMetadata metadata = MessagePool.getInstance().get(ITableMetadata.class);
        final List<ITableMetadata.IMetadataValue> metadataValues = metadata.getMetadataValues();

        for(String key : this.keySet()){
            final ITableMetadata.IValue valueMessage = MessagePool.getInstance().get(ITableMetadata.IValue.class);

            Object value = this.get(key);

            if(value instanceof String){
                valueMessage.setStringValue((String)value);
            }else if(value instanceof Integer){
                valueMessage.setIntegerValue((Integer)value);
            }else if(value instanceof Long){
                valueMessage.setLongValue((Long)value);
            } else if(value instanceof Double){
                valueMessage.setDoubleValue((Double)value);
            }else if(value instanceof Float){
                valueMessage.setFloatValue((Float)value);
            }else if(value instanceof Boolean){
                valueMessage.setBooleanValue((Boolean)value);
            }

            final ITableMetadata.IMetadataValue metadataValue = MessagePool.getInstance().get(ITableMetadata.IMetadataValue.class)
                    .setName(key)
                    .setValue(valueMessage);
            metadataValues.add(metadataValue);
        }

        return metadata;
    }

    public static TableMetaData fromDto(ITableMetadata tableMetaDataDto) {
        TableMetaData tableMetaData = new TableMetaData();

        List<ITableMetadata.IMetadataValue> metaDataValues = tableMetaDataDto.getMetadataValues();
        int count = metaDataValues.size();
        for (int i = 0; i < count; i++) {
            ITableMetadata.IMetadataValue metaDataValue = metaDataValues.get(i);
            tableMetaData.put(metaDataValue.getName(), metaDataValue.getValue());
        }

        return tableMetaData;
    }
}
