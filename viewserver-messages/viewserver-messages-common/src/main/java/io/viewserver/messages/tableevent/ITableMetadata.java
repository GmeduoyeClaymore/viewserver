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

package io.viewserver.messages.tableevent;

import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.IRecyclableMessage;
import io.viewserver.messages.common.ColumnType;

import java.util.List;

/**
 * Created by bemm on 02/12/15.
 */
public interface ITableMetadata<T> extends IPoolableMessage<T> {
    List<IMetadataValue> getMetadataValues();

    interface IMetadataValue<T> extends IRecyclableMessage<T> {
        String getName();

        IMetadataValue setName(String name);

        IValue getValue();

        IMetadataValue setValue(IValue value);
    }

    interface IValue<T> extends IPoolableMessage<T> {
        ColumnType getValueType();

        boolean getBooleanValue();
        IValue setBooleanValue(boolean value);
        int getIntegerValue();
        IValue setIntegerValue(int value);
        long getLongValue();
        IValue setLongValue(long value);
        float getFloatValue();
        IValue setFloatValue(float value);
        double getDoubleValue();
        IValue setDoubleValue(double value);
        String getStringValue();
        IValue setStringValue(String value);
    }
}
