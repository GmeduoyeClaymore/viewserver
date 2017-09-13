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

package io.viewserver.messages.command;

import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.IRecyclableMessage;

import java.util.List;

/**
 * Created by nick on 03/12/15.
 */
public interface IUpdateDimensionMapCommand<T> extends IPoolableMessage<T> {
    List<IDataSource> getDataSources();

    interface IDataSource<T> extends IRecyclableMessage<T> {
        String getName();
        IDataSource<T> setName(String name);

        List<IDimension> getDimensions();
    }

    interface IDimension<T> extends IRecyclableMessage<T> {
        String getName();
        IDimension setName(String name);

        List<IMapping> getMappings();
    }

    interface IMapping<T> extends IRecyclableMessage<T> {
        int getId();
        IMapping<T> setId(int id);

        boolean getBooleanValue();
        IMapping<T> setBooleanValue(boolean value);

        int getIntegerValue();
        IMapping<T> setIntegerValue(int value);

        long getLongValue();
        IMapping<T> setLongValue(long value);

        String getStringValue();
        IMapping<T> setStringValue(String value);

        boolean getNullValue();
        IMapping<T> setNullValue();
    }
}
