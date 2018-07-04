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

import io.viewserver.messages.PoolableMessage;
import io.viewserver.messages.config.IIndexConfig;
import io.viewserver.messages.protobuf.dto.IndexConfigMessage;

import java.util.List;

/**
 * Created by bemm on 08/12/15.
 */
public class IndexConfig extends PoolableMessage<IndexConfig> implements IIndexConfig<IndexConfig> {
    private IndexConfigMessage.IndexConfigDtoOrBuilder indexConfigDto;
    private RecyclingList<IOutput, IndexConfigMessage.IndexConfigDto.Output> outputs;

    IndexConfig() {
        super(IIndexConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        indexConfigDto = (IndexConfigMessage.IndexConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public List<IOutput> getOutputs() {
        if (outputs == null) {
            outputs = new RecyclingList<IOutput, IndexConfigMessage.IndexConfigDto.Output>(IOutput.class) {
                @Override
                protected void doAdd(Object dto) {
                    final IndexConfigMessage.IndexConfigDto.Builder builder = getIndexConfigDtoBuilder();
                    dtoList = builder.getOutputsList();
                    if (dto instanceof IndexConfigMessage.IndexConfigDto.Output) {
                        builder.addOutputs((IndexConfigMessage.IndexConfigDto.Output) dto);
                    } else {
                        builder.addOutputs((IndexConfigMessage.IndexConfigDto.Output.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getIndexConfigDtoBuilder().clearOutputs();
                }
            };
        }
        outputs.setDtoList(indexConfigDto != null ? indexConfigDto.getOutputsList() : null);
        return outputs;
    }

    @Override
    protected void doRelease() {
        if (outputs != null) {
            outputs.release();
        }
        indexConfigDto = null;
    }

    IndexConfigMessage.IndexConfigDto.Builder getBuilder() {
        return getIndexConfigDtoBuilder();
    }

    private IndexConfigMessage.IndexConfigDto.Builder getIndexConfigDtoBuilder() {
        if (indexConfigDto == null) {
            indexConfigDto = IndexConfigMessage.IndexConfigDto.newBuilder();
        } else if (indexConfigDto instanceof IndexConfigMessage.IndexConfigDto) {
            indexConfigDto = ((IndexConfigMessage.IndexConfigDto) indexConfigDto).toBuilder();
        }
        return (IndexConfigMessage.IndexConfigDto.Builder) indexConfigDto;
    }

    public static class Output extends PoolableMessage<Output> implements IOutput<Output> {
        private IndexConfigMessage.IndexConfigDto.OutputOrBuilder outputDto;
        private RecyclingList<IQueryHolder, IndexConfigMessage.IndexConfigDto.Output.QueryHolder> queryHolders;

        Output() {
            super(IOutput.class);
        }

        @Override
        public void setDto(Object dto) {
            outputDto = (IndexConfigMessage.IndexConfigDto.OutputOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return outputDto.getName();
        }

        @Override
        public IOutput<Output> setName(String name) {
            getOutputDtoBuilder().setName(name);
            return this;
        }

        @Override
        public List<IQueryHolder> getQueryHolders() {
            if (queryHolders == null) {
                queryHolders = new RecyclingList<IQueryHolder, IndexConfigMessage.IndexConfigDto.Output.QueryHolder>(IQueryHolder.class) {
                    @Override
                    protected void doAdd(Object dto) {
                        final IndexConfigMessage.IndexConfigDto.Output.Builder builder = getOutputDtoBuilder();
                        dtoList = builder.getQueryHoldersList();
                        if (dto instanceof IndexConfigMessage.IndexConfigDto.Output.QueryHolder) {
                            builder.addQueryHolders((IndexConfigMessage.IndexConfigDto.Output.QueryHolder) dto);
                        } else {
                            builder.addQueryHolders((IndexConfigMessage.IndexConfigDto.Output.QueryHolder.Builder) dto);
                        }
                    }

                    @Override
                    protected void doClear() {
                        getOutputDtoBuilder().clearQueryHolders();
                    }
                };
            }
            queryHolders.setDtoList(outputDto != null ? outputDto.getQueryHoldersList() : null);
            return queryHolders;
        }

        @Override
        protected void doRelease() {
            if (queryHolders != null) {
                queryHolders.release();
            }
            outputDto = null;
        }

        IndexConfigMessage.IndexConfigDto.Output.Builder getBuilder() {
            return getOutputDtoBuilder();
        }

        private IndexConfigMessage.IndexConfigDto.Output.Builder getOutputDtoBuilder() {
            if (outputDto == null) {
                outputDto = IndexConfigMessage.IndexConfigDto.Output.newBuilder();
            } else if (outputDto instanceof IndexConfigMessage.IndexConfigDto.Output) {
                outputDto = ((IndexConfigMessage.IndexConfigDto.Output) outputDto).toBuilder();
            }
            return (IndexConfigMessage.IndexConfigDto.Output.Builder) outputDto;
        }
    }

    public static class QueryHolder extends PoolableMessage<QueryHolder> implements IQueryHolder<QueryHolder> {
        private IndexConfigMessage.IndexConfigDto.Output.QueryHolderOrBuilder queryHolderDto;
        private ListWrapper<Integer> valuesList;

        QueryHolder() {
            super(IQueryHolder.class);
        }

        @Override
        public void setDto(Object dto) {
            queryHolderDto = (IndexConfigMessage.IndexConfigDto.Output.QueryHolderOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getColumnName() {
            return queryHolderDto.getColumnName();
        }

        @Override
        public IQueryHolder<QueryHolder> setColumnName(String columnName) {
            getQueryHolderDtoBuilder().setColumnName(columnName);
            return this;
        }

        @Override
        public List<Integer> getValues() {
            if (valuesList == null) {
                valuesList = new ListWrapper<>(x -> {
                    final IndexConfigMessage.IndexConfigDto.Output.QueryHolder.Builder builder = getQueryHolderDtoBuilder();
                    valuesList.setInnerList(builder.getValuesList());
                    builder.addValues(x);
                });
            }
            valuesList.setInnerList(queryHolderDto != null ? queryHolderDto.getValuesList() : null);
            return valuesList;
        }

        @Override
        public boolean isExclude() {
            return queryHolderDto.hasExclude() && queryHolderDto.getExclude();
        }

        @Override
        public IQueryHolder<QueryHolder> setExclude(boolean exclude) {
            getQueryHolderDtoBuilder().setExclude(exclude);
            return this;
        }

        @Override
        protected void doRelease() {
            if (valuesList != null) {
                valuesList.setInnerList(null);
            }
            queryHolderDto = null;
        }

        IndexConfigMessage.IndexConfigDto.Output.QueryHolder.Builder getBuilder() {
            return getQueryHolderDtoBuilder();
        }

        private IndexConfigMessage.IndexConfigDto.Output.QueryHolder.Builder getQueryHolderDtoBuilder() {
            if (queryHolderDto == null) {
                queryHolderDto = IndexConfigMessage.IndexConfigDto.Output.QueryHolder.newBuilder();
            } else if (queryHolderDto instanceof IndexConfigMessage.IndexConfigDto.Output.QueryHolder) {
                queryHolderDto = ((IndexConfigMessage.IndexConfigDto.Output.QueryHolder) queryHolderDto).toBuilder();
            }
            return (IndexConfigMessage.IndexConfigDto.Output.QueryHolder.Builder) queryHolderDto;
        }
    }
}
