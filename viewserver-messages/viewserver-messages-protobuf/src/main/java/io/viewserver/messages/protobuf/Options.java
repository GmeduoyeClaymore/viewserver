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

package io.viewserver.messages.protobuf;

import io.viewserver.messages.MessagePool;
import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.command.IOptions;
import io.viewserver.messages.protobuf.dto.OptionsMessage;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by nick on 03/12/15.
 */
public class Options extends PoolableMessage<Options> implements IOptions<Options> {
    private OptionsMessage.OptionsDtoOrBuilder optionsDto;
    private RecyclingList<ISortColumn, OptionsMessage.OptionsDto.SortColumn> sortColumnList;

    public static Options fromDto(OptionsMessage.OptionsDto optionsDto) {
        final Options options = (Options) MessagePool.getInstance().get(IOptions.class);
        options.optionsDto = optionsDto;
        return options;
    }

    Options() {
        super(IOptions.class);
    }

    @Override
    public int getOffset() {
        return optionsDto.getOffset();
    }

    @Override
    public IOptions setOffset(int offset) {
        getOptionsDtoBuilder().setOffset(offset);
        return this;
    }

    @Override
    public int getLimit() {
        return optionsDto.getLimit();
    }

    @Override
    public IOptions setLimit(int limit) {
        getOptionsDtoBuilder().setLimit(limit);
        return this;
    }

    @Override
    public String getRankColumnName() {
        return optionsDto.hasColumnName() ? optionsDto.getColumnName() : null;
    }

    @Override
    public IOptions setRankColumnName(String rankColumnName) {
        getOptionsDtoBuilder().setColumnName(rankColumnName);
        return this;
    }

    @Override
    public List<ISortColumn> getColumnsToSort() {
        if (sortColumnList == null) {
            sortColumnList = new RecyclingList<ISortColumn, OptionsMessage.OptionsDto.SortColumn>(ISortColumn.class) {
                @Override
                protected void doAdd(Object o) {
                    final OptionsMessage.OptionsDto.Builder builder = getOptionsDtoBuilder();
                    dtoList = builder.getColumnsToSortList();
                    if (o instanceof OptionsMessage.OptionsDto.SortColumn) {
                        builder.addColumnsToSort((OptionsMessage.OptionsDto.SortColumn) o);
                    } else {
                        builder.addColumnsToSort((OptionsMessage.OptionsDto.SortColumn.Builder) o);
                    }
                }

                @Override
                protected void doClear() {
                    getOptionsDtoBuilder().clearColumnsToSort();
                }
            };
        }
        sortColumnList.setDtoList(optionsDto != null ? optionsDto.getColumnsToSortList() : null);
        return sortColumnList;
    }

    @Override
    public FilterMode getFilterMode() {
        if (optionsDto.hasFilterMode()) {
            return null;
        }
        final OptionsMessage.FilterMode filterMode = optionsDto.getFilterMode();
        switch (filterMode) {
            case TRANSPARENT: {
                return FilterMode.Transparent;
            }
            case FILTERING: {
                return FilterMode.Filtering;
            }
            case OPAQUE: {
                return FilterMode.Opaque;
            }
            default: {
                throw new UnsupportedOperationException(String.format("Unknown filter mode '%s' in dto", filterMode));
            }
        }
    }

    @Override
    public IOptions setFilterMode(FilterMode filterMode) {
        switch (filterMode) {
            case Transparent: {
                getOptionsDtoBuilder().setFilterMode(OptionsMessage.FilterMode.TRANSPARENT);
                break;
            }
            case Filtering: {
                getOptionsDtoBuilder().setFilterMode(OptionsMessage.FilterMode.FILTERING);
                break;
            }
            case Opaque: {
                getOptionsDtoBuilder().setFilterMode(OptionsMessage.FilterMode.OPAQUE);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown filter mode '%s'", filterMode));
            }
        }
        return this;
    }

    @Override
    public String getFilterExpression() {
        return optionsDto.hasFilterExpression() ? optionsDto.getFilterExpression() : null;
    }

    @Override
    public IOptions setFilterExpression(String filterExpression) {
        getOptionsDtoBuilder().setFilterExpression(filterExpression);
        return this;
    }

    @Override
    public EnumSet<SubscriptionFlags> getFlags() {
        final List<OptionsMessage.SubscriptionFlags> flagsList = optionsDto.getFlagsList();
        final EnumSet<SubscriptionFlags> flags = EnumSet.noneOf(SubscriptionFlags.class);
        int count = flagsList.size();
        for (int i = 0; i < count; i++) {
            final OptionsMessage.SubscriptionFlags flag = flagsList.get(i);
            switch (flag) {
                case SNAPSHOT_ONLY: {
                    flags.add(SubscriptionFlags.SnapshotOnly);
                    break;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("Unknown subscription flag '%s'", flag));
                }
            }
        }
        return flags;
    }

    @Override
    public IOptions setFlags(SubscriptionFlags... flags) {
        final OptionsMessage.OptionsDto.Builder builder = getOptionsDtoBuilder();
        int count = flags.length;
        for (int i = 0; i < count; i++) {
            switch (flags[i]) {
                case SnapshotOnly: {
                    builder.addFlags(OptionsMessage.SubscriptionFlags.SNAPSHOT_ONLY);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Unknown subscription flag '%s'", flags[i]));
                }
            }
        }
        return this;
    }

    private OptionsMessage.OptionsDto.Builder getOptionsDtoBuilder() {
        if (optionsDto == null) {
            optionsDto = OptionsMessage.OptionsDto.newBuilder();
        } else if (optionsDto instanceof OptionsMessage.OptionsDto) {
            optionsDto = ((OptionsMessage.OptionsDto) optionsDto).toBuilder();
        }
        return (OptionsMessage.OptionsDto.Builder) optionsDto;
    }

    OptionsMessage.OptionsDto.Builder getBuilder() {
        final OptionsMessage.OptionsDto.Builder builder = getOptionsDtoBuilder();
        return builder;
    }

    @Override
    protected void doRelease() {
        if (sortColumnList != null) {
            sortColumnList.release();
        }
        optionsDto = null;
    }

    public static class SortColumn extends PoolableMessage<SortColumn> implements ISortColumn<SortColumn> {
        private OptionsMessage.OptionsDto.SortColumnOrBuilder sortColumnDto;

        public SortColumn() {
            super(ISortColumn.class);
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public void setDto(Object dto) {
            sortColumnDto = (OptionsMessage.OptionsDto.SortColumnOrBuilder) dto;
        }

        @Override
        public String getName() {
            return sortColumnDto.getName();
        }

        @Override
        public ISortColumn setName(String name) {
            getSortColumnDtoBuilder().setName(name);
            return this;
        }

        @Override
        public SortDirection getSortDirection() {
            final OptionsMessage.SortDirection direction = sortColumnDto.getDirection();
            switch (direction) {
                case ASCENDING: {
                    return SortDirection.Ascending;
                }
                case DESCENDING: {
                    return SortDirection.Descending;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("Unknown sort direction '%s' in dto", direction));
                }
            }
        }

        @Override
        public ISortColumn setSortDirection(SortDirection sortDirection) {
            final OptionsMessage.OptionsDto.SortColumn.Builder builder = getSortColumnDtoBuilder();
            switch (sortDirection) {
                case Ascending: {
                    builder.setDirection(OptionsMessage.SortDirection.ASCENDING);
                    break;
                }
                case Descending: {
                    builder.setDirection(OptionsMessage.SortDirection.DESCENDING);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Unknown sort direction '%s'", sortDirection));
                }
            }
            return this;
        }

        @Override
        protected void doRelease() {
            sortColumnDto = null;
        }

        OptionsMessage.OptionsDto.SortColumn.Builder getBuilder() {
            return getSortColumnDtoBuilder();
        }

        private OptionsMessage.OptionsDto.SortColumn.Builder getSortColumnDtoBuilder() {
            if (sortColumnDto == null) {
                sortColumnDto = OptionsMessage.OptionsDto.SortColumn.newBuilder();
            } else if (sortColumnDto instanceof OptionsMessage.OptionsDto.SortColumn) {
                sortColumnDto = ((OptionsMessage.OptionsDto.SortColumn) sortColumnDto).toBuilder();
            }
            return (OptionsMessage.OptionsDto.SortColumn.Builder) sortColumnDto;
        }
    }
}
