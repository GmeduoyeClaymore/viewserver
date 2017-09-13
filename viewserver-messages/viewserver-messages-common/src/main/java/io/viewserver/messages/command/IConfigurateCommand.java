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
import io.viewserver.messages.common.ColumnType;
import io.viewserver.messages.config.IOperatorConfig;

import java.util.List;

/**
 * Created by nick on 03/12/15.
 */
public interface IConfigurateCommand<T> extends IPoolableMessage<T> {
    List<IOperator> getOperators();

    interface IOperator<T> extends IRecyclableMessage<T> {
        String getName();

        IOperator<T> setName(String name);

        String getType();

        IOperator<T> setType(String type);

        Operation getOperation();

        IOperator<T> setOperation(Operation operation);

        boolean hasConfig();

        <T extends IOperatorConfig> T getConfig(Class<T> configClass);

        IOperator<T> setConfig(IOperatorConfig<?> config);

        List<IConnection> getConnections();

        List<IMetadataItem> getMetadataItems();
    }

    enum Operation {
        CreateConfigure,
        Remove
    }

    interface IConnection<T> extends IRecyclableMessage<T> {
        String getInput();
        IConnection<T> setInput(String input);

        String getOperator();
        IConnection<T> setOperator(String operator);

        String getOutput();
        IConnection<T> setOutput(String output);
    }

    interface IMetadataItem<T> extends IRecyclableMessage<T> {
        String getKey();
        IMetadataItem<T> setKey(String key);

        ColumnType getType();

        boolean getBooleanValue();
        IMetadataItem<T> setBooleanValue(boolean value);

        int getIntegerValue();
        IMetadataItem<T> setIntegerValue(int value);

        long getLongValue();
        IMetadataItem<T> setLongValue(long value);

        float getFloatValue();
        IMetadataItem<T> setFloatValue(float value);

        double getDoubleValue();
        IMetadataItem<T> setDoubleValue(double value);

        String getStringValue();
        IMetadataItem<T> setStringValue(String value);
    }
}
