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

import io.viewserver.core.JacksonSerialiser;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IOptions;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.operators.sort.SortOperator;
import io.viewserver.subscription.SubscriptionFlags;
import com.google.common.collect.Collections2;

import java.util.*;

public class Options {
    private int offset;
    private int limit;
    private final List<SortOperator.SortColumn> columnsToSort = new ArrayList<>();
    private String columnName;
    private FilterOperator.FilterMode filterMode = FilterOperator.FilterMode.Transparent;
    private String filterExpression;
    private EnumSet<SubscriptionFlags> flags = EnumSet.noneOf(SubscriptionFlags.class);

    public int getOffset() {
        return offset;
    }

    public void addSortColumn(String columnName, boolean descending) {
        columnsToSort.add(new SortOperator.SortColumn(columnName, descending));
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public FilterOperator.FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterOperator.FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public void setFlags(SubscriptionFlags... flags) {
        this.flags.addAll(Arrays.asList(flags));
    }

    public void clearFlags(SubscriptionFlags... flags) {
        if (flags.length == 0) {
            this.flags.clear();
        } else {
            this.flags.removeAll(Arrays.asList(flags));
        }
    }

    public boolean hasFlag(SubscriptionFlags flag) {
        return this.flags.contains(flag);
    }

    public static Options fromMessage(IOptions dtoOptions) {
        Options options = new Options();
        options.offset = dtoOptions.getOffset();
        options.limit = dtoOptions.getLimit();
        final String rankColumnName = dtoOptions.getRankColumnName();
        options.columnName = rankColumnName != null ? rankColumnName : "rank";
        List<IOptions.ISortColumn> sortColumns = dtoOptions.getColumnsToSort();
        int count = sortColumns.size();
        for (int i = 0; i < count; i++) {
            IOptions.ISortColumn columnToSort = sortColumns.get(i);
            options.columnsToSort.add(new SortOperator.SortColumn(columnToSort.getName(),
                    columnToSort.getSortDirection().equals(IOptions.SortDirection.Descending)));
        }
        final IOptions.FilterMode filterMode = dtoOptions.getFilterMode();
        if (filterMode != null) {
            switch (filterMode) {
                case Transparent: {
                    options.filterMode = FilterOperator.FilterMode.Transparent;
                    break;
                }
                case Filtering: {
                    options.filterMode = FilterOperator.FilterMode.Filter;
                    break;
                }
                case Opaque: {
                    options.filterMode = FilterOperator.FilterMode.Opaque;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid filter mode '" + filterMode + "'");
                }
            }
        }
        options.filterExpression = dtoOptions.getFilterExpression();

        EnumSet<IOptions.SubscriptionFlags> flagsList = dtoOptions.getFlags();
        for (IOptions.SubscriptionFlags subscriptionFlag : flagsList) {
            switch (subscriptionFlag) {
                case SnapshotOnly: {
                    options.setFlags(SubscriptionFlags.SnapshotOnly);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown subscription flag '" + subscriptionFlag + "' in options DTO");
                }
            }
        }

        return options;
    }

    public IOptions toMessage() {
        IOptions message = MessagePool.getInstance().get(IOptions.class)
                .setOffset(offset)
                .setLimit(limit)
                .setRankColumnName(columnName != null ? columnName : "rank");
        Collection<IOptions.ISortColumn> sortColumns = Collections2.transform(columnsToSort, sortColumn ->
                        MessagePool.getInstance().get(IOptions.ISortColumn.class)
                                .setName(sortColumn.getName())
                                .setSortDirection(sortColumn.isDescending() ? IOptions.SortDirection.Descending : IOptions.SortDirection.Ascending)
        );
        message.getColumnsToSort().addAll(sortColumns);
        if (filterMode != null) {
            switch (filterMode) {
                case Transparent: {
                    message.setFilterMode(IOptions.FilterMode.Transparent);
                    break;
                }
                case Filter: {
                    message.setFilterMode(IOptions.FilterMode.Filtering);
                    break;
                }
                case Opaque: {
                    message.setFilterMode(IOptions.FilterMode.Opaque);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Cannot map filter mode '" + filterMode + "'");
                }
            }
        }
        if (filterExpression != null) {
            message.setFilterExpression(filterExpression);
        }
        for (SubscriptionFlags flag : this.flags) {
            switch (flag) {
                case SnapshotOnly: {
                    message.setFlags(IOptions.SubscriptionFlags.SnapshotOnly);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Cannot map subscription flag '" + flag + "'");
                }
            }
        }

        return message;
    }

    public List<SortOperator.SortColumn> getColumnsToSort() {
        return columnsToSort;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public String toString() {
        JacksonSerialiser serialiser = new JacksonSerialiser();
        return serialiser.serialise(this, true);
    }
}
