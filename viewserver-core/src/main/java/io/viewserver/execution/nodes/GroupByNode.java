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

package io.viewserver.execution.nodes;

import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.group.IGroupByConfig;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by nickc on 01/11/2014.
 */
public class GroupByNode extends GraphNodeBase<GroupByNode> implements ICoalesceableGraphNode {
    private List<String> groupByColumns = new ArrayList<>();
    private List<SummaryDefinition> summaries = new ArrayList<>();
    private String countColumnName;
    private List<String> subtotals = new ArrayList<>();

    public GroupByNode() {
        super();
    }

    public GroupByNode(String name) {
        super(name, "GroupBy");
    }

    public GroupByNode withGroupByColumns(String... groupByColumns) {
        this.groupByColumns.addAll(Arrays.asList(groupByColumns));
        return this;
    }

    public GroupByNode withSummary(String name, String function, String target) {
        return withSummary(new SummaryDefinition(name, function, target));
    }

    public GroupByNode withSummary(String name, String function, String target, Object[] arguments) {
        return withSummary(new SummaryDefinition(name, function, target, arguments));
    }

    public GroupByNode withSummary(SummaryDefinition summary) {
        int count = summaries.size();
        for (int i = 0; i < count; i++) {
            if (summary.equals(summaries.get(i))) {
                return this;
            }
        }
        summaries.add(summary);
        return this;
    }

    public GroupByNode withRegexSummary(String name, String function, String target) {
        summaries.add(new SummaryDefinition(name, function, target, true));
        return this;
    }

    public GroupByNode withRegexSummary(String name, String function, String target, Object[] arguments) {
        summaries.add(new SummaryDefinition(name, function, target, true, arguments));
        return this;
    }

    public GroupByNode withCountColumnName(String countColumnName) {
        this.countColumnName = countColumnName;
        return this;
    }

    public GroupByNode withSubtotals(String... subtotals) {
        this.subtotals.addAll(Arrays.asList(subtotals));
        return this;
    }

    public GroupByNode withSubtotals(Collection<String> subtotals) {
        this.subtotals.addAll(subtotals);
        return this;
    }

    @Override
    public Object getConfig(final ParameterHelper parameterHelper) {
        return new IGroupByConfig() {
            @Override
            public List<String> getGroupBy() {
                List<String> groupBy = new ArrayList<>();
                int count = GroupByNode.this.groupByColumns.size();
                for (int i = 0; i < count; i++) {
                    String values = parameterHelper.substituteParameterValues(GroupByNode.this.groupByColumns.get(i));

                    if (values != null) {
                        groupBy.addAll(Arrays.asList(values.split(",")));
                    }
                }
                return groupBy;
            }

            @Override
            public List<Summary> getSummaries() {
                ArrayList<Summary> summaries = new ArrayList<>();
                List<SummaryDefinition> summaryDefinitions = GroupByNode.this.getSummaries();
                int count = summaryDefinitions.size();
                for (int i = 0; i < count; i++) {
                    SummaryDefinition summary = summaryDefinitions.get(i);
                    summaries.add(new Summary(parameterHelper.substituteParameterValues(summary.name),
                            summary.function,
                            parameterHelper.substituteParameterValues(summary.target),
                            summary.isRegex,
                            summary.arguments));
                }

                return summaries;
            }

            @Override
            public String getCountColumnName() {
                return countColumnName;
            }

            @Override
            public List<String> getSubtotals() {
                List<String> subtotals = new ArrayList<>();
                for (String column : GroupByNode.this.subtotals) {
                    String values = parameterHelper.substituteParameterValues(column);

                    if (values != null) {
                        subtotals.addAll(Arrays.asList(values.split(",")));
                    }
                }
                return subtotals;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        io.viewserver.messages.config.IGroupByConfig builder = MessagePool.getInstance().get(io.viewserver.messages.config.IGroupByConfig.class);
        int count = groupByColumns.size();
        final List<String> groupBy = builder.getGroupBy();
        for (int i = 0; i < count; i++) {
            String values = parameterHelper.substituteParameterValues(groupByColumns.get(i));
            if (values != null) {
                groupBy.addAll(Arrays.asList(values.split(",")));
            }
        }
        count = summaries.size();
        final List<io.viewserver.messages.config.IGroupByConfig.ISummary> summaryMessages = builder.getSummaries();
        for (int i = 0; i < count; i++) {
            final SummaryDefinition summary = this.summaries.get(i);
            io.viewserver.messages.config.IGroupByConfig.ISummary summaryDto = MessagePool.getInstance().get(io.viewserver.messages.config.IGroupByConfig.ISummary.class)
                    .setName(parameterHelper.substituteParameterValues(summary.getName()))
                    .setTarget(parameterHelper.substituteParameterValues(summary.getTarget()))
                    .setFunction(summary.getFunction());
            final List<String> arguments = summaryDto.getArguments();
            if (summary.arguments != null) {
                int argCount = summary.arguments.length;
                for (int j = 0; j < argCount; j++) {
                    arguments.add(summary.arguments[j].toString());
                }
            }
            summaryMessages.add(summaryDto);
            summaryDto.release();
        }
        if (countColumnName != null) {
            builder.setCountColumnName(countColumnName);
        }
        count = subtotals.size();
        for (int i = 0; i < count; i++) {
            String values = parameterHelper.substituteParameterValues(subtotals.get(i));

            if (values != null) {
                builder.getSubtotals().addAll(Arrays.asList(values.split(",")));
            }
        }
        return builder;
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        ArrayList<String> summaryDefs = new ArrayList<>();
        ArrayList<String> groupByCols = new ArrayList<>();
        ArrayList<String> subTotalsArray = new ArrayList<>();

        for (SummaryDefinition summaryDefinition : summaries) {
            summaryDefs.add(String.format("%s:%s:%s:%b:[%s]",
                    parameterHelper.substituteParameterValues(summaryDefinition.name),
                    summaryDefinition.function,
                    parameterHelper.substituteParameterValues(summaryDefinition.target),
                    summaryDefinition.isRegex,
                    StringUtils.join(summaryDefinition.arguments, ',')));
        }

        for (String groupByColumn : groupByColumns) {
            groupByCols.add(parameterHelper.substituteParameterValues(groupByColumn));
        }

        for (String subTotal : subtotals) {
            subTotalsArray.add(parameterHelper.substituteParameterValues(subTotal));
        }

        return String.format("groupBy:%s:%s:%s", StringUtils.join(groupByCols, ','), StringUtils.join(summaryDefs, ','),
                StringUtils.join(subTotalsArray, ','));
    }

    public List<SummaryDefinition> getSummaries() {
        return summaries;
    }

    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    public void setGroupByColumns(List<String> groupByColumns) {
        this.groupByColumns = groupByColumns;
    }

    public void setSummaries(List<SummaryDefinition> summaries) {
        this.summaries = summaries;
    }

    public List<String> getSubtotals() {
        return subtotals;
    }

    public void setSubtotals(List<String> subtotals) {
        this.subtotals = subtotals;
    }

    @Override
    public IGraphNode copyForCoalescing() {
        // when coalescing, we want to retain the summaries as they are (before they become distributed)
        GroupByNode copy = new GroupByNode();
        super.copyTo(copy);
        copy.setGroupByColumns(groupByColumns);
        copy.setSubtotals(subtotals);
        copy.setSummaries(new ArrayList<>(summaries));
        return copy;
    }

    public static class SummaryDefinition {
        private String name;
        private String function;
        private String target;
        private boolean isRegex;
        private Object[] arguments;

        public SummaryDefinition() {
        }

        public SummaryDefinition(String name, String function, String target) {
            this.name = name;
            this.function = function;
            this.target = target;
        }

        public SummaryDefinition(String name, String function, String target, Object[] arguments) {
            this.name = name;
            this.function = function;
            this.target = target;
            this.arguments = arguments;
        }

        public SummaryDefinition(String name, String function, String target, boolean isRegex) {
            this.name = name;
            this.function = function;
            this.target = target;
            this.isRegex = isRegex;
        }

        public SummaryDefinition(String name, String function, String target, boolean isRegex, Object[] arguments) {
            this.name = name;
            this.function = function;
            this.target = target;
            this.isRegex = isRegex;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public String getFunction() {
            return function;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFunction(String function) {
            this.function = function;
        }

        public void setArguments(Object[] arguments) {
            this.arguments = arguments;
        }

        @Override
        public String toString() {
            return String.format("%s:%s:[%s]", name, function, StringUtils.join(arguments, ','));
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public boolean isRegex() {
            return isRegex;
        }

        public void setRegex(boolean isRegex) {
            this.isRegex = isRegex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SummaryDefinition that = (SummaryDefinition) o;

            if (isRegex != that.isRegex) return false;
            if (!name.equals(that.name)) return false;
            if (!function.equals(that.function)) return false;
            if (!target.equals(that.target)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(arguments, that.arguments);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + function.hashCode();
            result = 31 * result + target.hashCode();
            result = 31 * result + (isRegex ? 1 : 0);
            result = 31 * result + (arguments != null ? Arrays.hashCode(arguments) : 0);
            return result;
        }
    }
}
