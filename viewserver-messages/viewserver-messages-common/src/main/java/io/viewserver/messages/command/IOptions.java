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

import java.util.EnumSet;
import java.util.List;

/**
 * Created by nick on 02/12/15.
 */
public interface IOptions<T> extends IPoolableMessage<T> {
    int getOffset();

    IOptions setOffset(int offset);

    int getLimit();

    IOptions setLimit(int limit);

    String getRankColumnName();

    IOptions setRankColumnName(String rankColumnName);

    List<ISortColumn> getColumnsToSort();

    FilterMode getFilterMode();

    IOptions setFilterMode(FilterMode filterMode);

    String getFilterExpression();

    IOptions setFilterExpression(String filterExpression);

    EnumSet<SubscriptionFlags> getFlags();

    IOptions setFlags(SubscriptionFlags... flags);

    interface ISortColumn<T> extends IPoolableMessage<T>, IRecyclableMessage<T> {
        String getName();

        ISortColumn setName(String name);

        SortDirection getSortDirection();

        ISortColumn setSortDirection(SortDirection sortDirection);
    }

    enum SortDirection {
        Ascending,
        Descending;
    }

    enum FilterMode {
        Transparent,
        Filtering,
        Opaque;
    }

    enum SubscriptionFlags {
        SnapshotOnly;
    }
}
