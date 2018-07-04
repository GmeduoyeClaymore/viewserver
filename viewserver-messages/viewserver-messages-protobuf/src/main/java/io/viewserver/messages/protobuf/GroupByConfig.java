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
import io.viewserver.messages.config.IGroupByConfig;
import io.viewserver.messages.protobuf.dto.GroupByConfigMessage;

import java.util.List;

/**
 * Created by bemm on 08/12/15.
 */
public class GroupByConfig extends PoolableMessage<GroupByConfig> implements IGroupByConfig<GroupByConfig> {
    private GroupByConfigMessage.GroupByConfigDtoOrBuilder groupByConfigDto;
    private ListWrapper<String> groupByList;
    private RecyclingList<ISummary, GroupByConfigMessage.GroupByConfigDto.Summary> summariesList;
    private ListWrapper<String> subtotalsList;

    GroupByConfig() {
        super(IGroupByConfig.class);
    }

    @Override
    public void setDto(Object dto) {
        groupByConfigDto = (GroupByConfigMessage.GroupByConfigDtoOrBuilder) dto;
    }

    @Override
    public Object getDto() {
        return getBuilder();
    }

    @Override
    public List<String> getGroupBy() {
        if (groupByList == null) {
            groupByList = new ListWrapper<>(x -> {
                final GroupByConfigMessage.GroupByConfigDto.Builder builder = getGroupByConfigDtoBuilder();
                groupByList.setInnerList(builder.getGroupByColumnList());
                builder.addGroupByColumn(x);
            });
        }
        groupByList.setInnerList(groupByConfigDto != null ? groupByConfigDto.getGroupByColumnList() : null);
        return groupByList;
    }

    @Override
    public List<ISummary> getSummaries() {
        if (summariesList == null) {
            summariesList = new RecyclingList<ISummary, GroupByConfigMessage.GroupByConfigDto.Summary>(ISummary.class) {
                @Override
                protected void doAdd(Object dto) {
                    final GroupByConfigMessage.GroupByConfigDto.Builder builder = getGroupByConfigDtoBuilder();
                    dtoList = builder.getSummariesList();
                    if (dto instanceof GroupByConfigMessage.GroupByConfigDto.Summary) {
                        builder.addSummaries((GroupByConfigMessage.GroupByConfigDto.Summary) dto);
                    } else {
                        builder.addSummaries((GroupByConfigMessage.GroupByConfigDto.Summary.Builder) dto);
                    }
                }

                @Override
                protected void doClear() {
                    getGroupByConfigDtoBuilder().clearSummaries();
                }
            };
        }
        summariesList.setDtoList(groupByConfigDto != null ? groupByConfigDto.getSummariesList() : null);
        return summariesList;
    }

    @Override
    public String getCountColumnName() {
        return groupByConfigDto.hasCountColumnName() ? groupByConfigDto.getCountColumnName() : null;
    }

    @Override
    public IGroupByConfig<GroupByConfig> setCountColumnName(String countColumnName) {
        getGroupByConfigDtoBuilder().setCountColumnName(countColumnName);
        return this;
    }

    @Override
    public List<String> getSubtotals() {
        if (subtotalsList == null) {
            subtotalsList = new ListWrapper<>(x -> {
                final GroupByConfigMessage.GroupByConfigDto.Builder builder = getGroupByConfigDtoBuilder();
                subtotalsList.setInnerList(builder.getSubtotalsList());
                builder.addSubtotals(x);
            });
        }
        subtotalsList.setInnerList(groupByConfigDto != null ? groupByConfigDto.getSubtotalsList() : null);
        return subtotalsList;
    }

    @Override
    protected void doRelease() {
        if (groupByList != null) {
            groupByList.setInnerList(null);
        }
        if (summariesList != null) {
            summariesList.release();
        }
        if (subtotalsList != null) {
            subtotalsList.setInnerList(null);
        }
        groupByConfigDto = null;
    }

    GroupByConfigMessage.GroupByConfigDto.Builder getBuilder() {
        return getGroupByConfigDtoBuilder();
    }

    private GroupByConfigMessage.GroupByConfigDto.Builder getGroupByConfigDtoBuilder() {
        if (groupByConfigDto == null) {
            groupByConfigDto = GroupByConfigMessage.GroupByConfigDto.newBuilder();
        } else if (groupByConfigDto instanceof GroupByConfigMessage.GroupByConfigDto) {
            groupByConfigDto = ((GroupByConfigMessage.GroupByConfigDto) groupByConfigDto).toBuilder();
        }
        return (GroupByConfigMessage.GroupByConfigDto.Builder) groupByConfigDto;
    }

    public static class Summary extends PoolableMessage<Summary> implements ISummary<Summary> {
        private GroupByConfigMessage.GroupByConfigDto.SummaryOrBuilder summaryDto;
        private ListWrapper<String> argumentsList;

        Summary() {
            super(ISummary.class);
        }

        @Override
        public void setDto(Object dto) {
            summaryDto = (GroupByConfigMessage.GroupByConfigDto.SummaryOrBuilder) dto;
        }

        @Override
        public Object getDto() {
            return getBuilder();
        }

        @Override
        public String getName() {
            return summaryDto.getName();
        }

        @Override
        public ISummary<Summary> setName(String name) {
            getSummaryDtoBuilder().setName(name);
            return this;
        }

        @Override
        public String getFunction() {
            return summaryDto.getFunction();
        }

        @Override
        public ISummary<Summary> setFunction(String function) {
            getSummaryDtoBuilder().setFunction(function);
            return this;
        }

        @Override
        public String getTarget() {
            return summaryDto.getTarget();
        }

        @Override
        public ISummary<Summary> setTarget(String target) {
            getSummaryDtoBuilder().setTarget(target);
            return this;
        }

        @Override
        public boolean isRegex() {
            return summaryDto.hasIsRegex() && summaryDto.getIsRegex();
        }

        @Override
        public ISummary<Summary> setIsRegex(boolean isRegex) {
            getSummaryDtoBuilder().setIsRegex(isRegex);
            return this;
        }

        @Override
        public List<String> getArguments() {
            if (argumentsList == null) {
                argumentsList = new ListWrapper<>(x -> {
                    final GroupByConfigMessage.GroupByConfigDto.Summary.Builder builder = getSummaryDtoBuilder();
                    argumentsList.setInnerList(builder.getArgumentsList());
                    builder.addArguments(x);
                });
            }
            argumentsList.setInnerList(summaryDto != null ? summaryDto.getArgumentsList() : null);
            return argumentsList;
        }

        @Override
        protected void doRelease() {
            if (argumentsList != null) {
                argumentsList.setInnerList(null);
            }
            summaryDto = null;
        }

        GroupByConfigMessage.GroupByConfigDto.Summary.Builder getBuilder() {
            return getSummaryDtoBuilder();
        }

        private GroupByConfigMessage.GroupByConfigDto.Summary.Builder getSummaryDtoBuilder() {
            if (summaryDto == null) {
                summaryDto = GroupByConfigMessage.GroupByConfigDto.Summary.newBuilder();
            } else if (summaryDto instanceof GroupByConfigMessage.GroupByConfigDto.Summary) {
                summaryDto = ((GroupByConfigMessage.GroupByConfigDto.Summary) summaryDto).toBuilder();
            }
            return (GroupByConfigMessage.GroupByConfigDto.Summary.Builder) summaryDto;
        }
    }
}
