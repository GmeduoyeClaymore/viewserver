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
import io.viewserver.messages.config.ISortConfig;
import io.viewserver.messages.protobuf.dto.SortConfigMessage;

import java.util.List;

/**
 * Created by bemm on 08/12/15.
 */
public class SortConfig extends PoolableMessage<SortConfig> implements ISortConfig<SortConfig> {
    private SortConfigMessage.SortConfigDtoOrBuilder sortConfigDto;
    private SortDescriptor sortDescriptor;

    SortConfig() {
        super(ISortConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        sortConfigDto = (SortConfigMessage.SortConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public ISortDescriptor getSortDescriptor() {
        if (sortDescriptor == null) {
            sortDescriptor = sortConfigDto != null ? SortDescriptor.fromDto(sortConfigDto.getSortDescriptor())
                    : (SortDescriptor) MessagePool.getInstance().get(ISortDescriptor.class);
        }
        return sortDescriptor;
    }

    @Override
    public ISortConfig<SortConfig> setSortDescriptor(ISortDescriptor sortDescriptor) {
        if (this.sortDescriptor != null) {
            this.sortDescriptor.release();
        }
        this.sortDescriptor = (SortDescriptor) sortDescriptor.retain();
        return this;
    }

    @Override
    public int getStart() {
        return sortConfigDto.getStart();
    }

    @Override
    public ISortConfig<SortConfig> setStart(int start) {
        getSortConfigDtoBuilder().setStart(start);
        return this;
    }

    @Override
    public int getEnd() {
        return sortConfigDto.getEnd();
    }

    @Override
    public ISortConfig<SortConfig> setEnd(int end) {
        getSortConfigDtoBuilder().setEnd(end);
        return this;
    }

    @Override
    protected void doRelease() {
        if (sortDescriptor != null) {
            sortDescriptor.release();
            sortDescriptor = null;
        }
        sortConfigDto = null;
    }

    SortConfigMessage.SortConfigDto.Builder getBuilder() {
        final SortConfigMessage.SortConfigDto.Builder builder = getSortConfigDtoBuilder();
        if (sortDescriptor != null) {
            builder.setSortDescriptor(sortDescriptor.getBuilder());
        }
        return builder;
    }

    private SortConfigMessage.SortConfigDto.Builder getSortConfigDtoBuilder() {
        if (sortConfigDto == null) {
            sortConfigDto = SortConfigMessage.SortConfigDto.newBuilder();
        } else if (sortConfigDto instanceof SortConfigMessage.SortConfigDto) {
            sortConfigDto = ((SortConfigMessage.SortConfigDto) sortConfigDto).toBuilder();
        }
        return (SortConfigMessage.SortConfigDto.Builder) sortConfigDto;
    }

    public static class SortDescriptor extends PoolableMessage<SortDescriptor> implements ISortDescriptor<SortDescriptor> {
        private SortConfigMessage.SortConfigDto.SortDescriptorOrBuilder sortDescriptorDto;
        private RecyclingList<ISortColumn, SortConfigMessage.SortConfigDto.SortColumn> sortColumns;

        public static SortDescriptor fromDto(SortConfigMessage.SortConfigDto.SortDescriptor dto) {
            final SortDescriptor sortDescriptor = (SortDescriptor) MessagePool.getInstance().get(ISortDescriptor.class);
            sortDescriptor.sortDescriptorDto = dto;
            return sortDescriptor;
        }

        SortDescriptor() {
            super(ISortDescriptor.class);
        }

        @Override
        public String getColumnName() {
            return sortDescriptorDto.getColumnName();
        }

        @Override
        public ISortDescriptor<SortDescriptor> setColumnName(String columnName) {
            getSortDescriptorDtoBuilder().setColumnName(columnName);
            return this;
        }

        @Override
        public List<ISortColumn> getColumnsToSort() {
            if (sortColumns == null) {
                sortColumns = new RecyclingList<ISortColumn, SortConfigMessage.SortConfigDto.SortColumn>(ISortColumn.class) {
                    @Override
                    protected void doAdd(Object dto) {
                        final SortConfigMessage.SortConfigDto.SortDescriptor.Builder builder = getSortDescriptorDtoBuilder();
                        dtoList = builder.getColumnsToSortList();
                        if (dto instanceof SortConfigMessage.SortConfigDto.SortColumn) {
                            builder.addColumnsToSort((SortConfigMessage.SortConfigDto.SortColumn) dto);
                        } else {
                            builder.addColumnsToSort((SortConfigMessage.SortConfigDto.SortColumn.Builder) dto);
                        }
                    }

                    @Override
                    protected void doClear() {
                        getSortDescriptorDtoBuilder().clearColumnsToSort();
                    }
                };
            }
            sortColumns.setDtoList(sortDescriptorDto != null ? sortDescriptorDto.getColumnsToSortList() : null);
            return sortColumns;
        }

        @Override
        protected void doRelease() {
            if (sortColumns != null) {
                sortColumns.release();
            }
            sortDescriptorDto = null;
        }

        SortConfigMessage.SortConfigDto.SortDescriptor.Builder getBuilder() {
            return getSortDescriptorDtoBuilder();
        }

        private SortConfigMessage.SortConfigDto.SortDescriptor.Builder getSortDescriptorDtoBuilder() {
            if (sortDescriptorDto == null) {
                sortDescriptorDto = SortConfigMessage.SortConfigDto.SortDescriptor.newBuilder();
            } else if (sortDescriptorDto instanceof SortConfigMessage.SortConfigDto.SortDescriptor) {
                sortDescriptorDto = ((SortConfigMessage.SortConfigDto.SortDescriptor) sortDescriptorDto).toBuilder();
            }
            return (SortConfigMessage.SortConfigDto.SortDescriptor.Builder) sortDescriptorDto;
        }
    }

    public static class SortColumn extends PoolableMessage<SortColumn> implements ISortColumn<SortColumn> {
        private SortConfigMessage.SortConfigDto.SortColumnOrBuilder sortColumnDto;

        SortColumn() {
            super(ISortColumn.class);
        }

        @Override
        public void setDto(Object dto) {
            sortColumnDto = (SortConfigMessage.SortConfigDto.SortColumnOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return sortColumnDto.getName();
        }

        @Override
        public ISortColumn<SortColumn> setName(String name) {
            getSortColumnDtoBuilder().setName(name);
            return this;
        }

        @Override
        public boolean isDescending() {
            return sortColumnDto.getDescending();
        }

        @Override
        public ISortColumn<SortColumn> setDescending(boolean descending) {
            getSortColumnDtoBuilder().setDescending(descending);
            return this;
        }

        @Override
        protected void doRelease() {
            sortColumnDto = null;
        }

        SortConfigMessage.SortConfigDto.SortColumn.Builder getBuilder() {
            return getSortColumnDtoBuilder();
        }

        private SortConfigMessage.SortConfigDto.SortColumn.Builder getSortColumnDtoBuilder() {
            if (sortColumnDto == null) {
                sortColumnDto = SortConfigMessage.SortConfigDto.SortColumn.newBuilder();
            } else if (sortColumnDto instanceof SortConfigMessage.SortConfigDto.SortColumn) {
                sortColumnDto = ((SortConfigMessage.SortConfigDto.SortColumn) sortColumnDto).toBuilder();
            }
            return (SortConfigMessage.SortConfigDto.SortColumn.Builder) sortColumnDto;
        }
    }
}
