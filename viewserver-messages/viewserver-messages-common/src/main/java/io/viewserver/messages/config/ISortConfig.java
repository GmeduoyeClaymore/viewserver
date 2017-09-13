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

package io.viewserver.messages.config;

import io.viewserver.messages.IPoolableMessage;
import io.viewserver.messages.IRecyclableMessage;

import java.util.List;

/**
 * Created by nick on 08/12/15.
 */
public interface ISortConfig<T> extends IOperatorConfig<T> {
    ISortDescriptor<T> getSortDescriptor();
    ISortConfig<T> setSortDescriptor(ISortDescriptor sortDescriptor);

    int getStart();
    ISortConfig<T> setStart(int start);

    int getEnd();
    ISortConfig<T> setEnd(int end);

    interface ISortColumn<T> extends IRecyclableMessage<T> {
        String getName();
        ISortColumn<T> setName(String name);

        boolean isDescending();
        ISortColumn<T> setDescending(boolean descending);
    }

    interface ISortDescriptor<T> extends IPoolableMessage<T> {
        String getColumnName();
        ISortDescriptor<T> setColumnName(String columnName);

        List<ISortColumn> getColumnsToSort();
    }
}
