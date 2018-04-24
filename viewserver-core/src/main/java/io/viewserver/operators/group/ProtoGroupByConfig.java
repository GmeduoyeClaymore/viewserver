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

package io.viewserver.operators.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bemm on 25/11/2014.
 */
public class ProtoGroupByConfig implements IGroupByConfig {
    private final List<String> groupBy;
    private final String countColumnName;
    private final List<String> subtotals;
    private List<Summary> summaries = new ArrayList<>();

    public ProtoGroupByConfig(io.viewserver.messages.config.IGroupByConfig groupByConfigDto) {
        this.groupBy = new ArrayList<>(groupByConfigDto.getGroupBy());

        final List<io.viewserver.messages.config.IGroupByConfig.ISummary> summaries = groupByConfigDto.getSummaries();
        int count = summaries.size();
        for (int i = 0; i < count; i++) {
            final io.viewserver.messages.config.IGroupByConfig.ISummary summary = summaries.get(i);
            final List<String> argumentsList = summary.getArguments();
            int argCount = argumentsList.size();
            Object[] arguments = new Object[argCount];
            for (int j = 0; j < argCount; j++) {
                arguments[j] = argumentsList.get(j);
            }
            this.summaries.add(new Summary(summary.getName(), summary.getFunction(), summary.getTarget(), summary.isRegex(), arguments));
            summary.release();
        }

        this.countColumnName = groupByConfigDto.getCountColumnName();
        this.subtotals = new ArrayList<>(groupByConfigDto.getSubtotals());
    }

    @Override
    public List<String> getGroupBy() {
        return groupBy;
    }

    @Override
    public List<Summary> getSummaries() {
        return summaries;
    }

    @Override
    public String getCountColumnName() {
        return countColumnName;
    }

    @Override
    public List<String> getSubtotals() {
        return subtotals;
    }

}
