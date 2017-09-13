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
import io.viewserver.messages.common.ValueLists;

import java.util.List;

/**
 * Created by nick on 03/12/15.
 */
public interface IReportContext<T> extends IRecyclableMessage<T> {
    String getReportId();
    IReportContext<T> setReportId(String reportId);

    List<IParameterValue> getParameterValues();

    List<IDimensionValue> getDimensionValues();

    List<IReportContext> getChildContexts();

    String getOutput();
    IReportContext<T> setOutput(String output);

    List<IDimensionValue> getExcludedFilters();

    String getMultiContextMode();
    IReportContext<T> setMultiContextMode(String multiContextMode);

    interface IValue<T> extends IPoolableMessage<T> {
        ColumnType getType();
        ValueLists.IBooleanList getBooleanValues();
        ValueLists.IIntegerList getIntegerValues();
        ValueLists.ILongList getLongValues();
        ValueLists.IFloatList getFloatValues();
        ValueLists.IDoubleList getDoubleValues();
        ValueLists.IStringList getStringValues();
    }

    interface IParameterValue<T> extends IRecyclableMessage<T> {
        String getName();
        IParameterValue<T> setName(String name);

        IValue getValue();
        IParameterValue<T> setValue(IValue value);
    }

    interface IDimensionValue<T> extends IRecyclableMessage<T> {
        String getName();
        IDimensionValue<T> setName(String name);

        IValue getValue();
        IDimensionValue<T> setValue(IValue value);
    }
}
