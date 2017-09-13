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

package io.viewserver.distribution;

import io.viewserver.datasource.ColumnType;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.group.IGroupByConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Created by nickc on 10/12/2014.
 */
public class DistributedSummaryHelper {
    public static List<CalcColOperator.CalculatedColumn> getPreSummaryCalculationsForPartition(GroupByNode.SummaryDefinition summaryDefinition, ParameterHelper parameterHelper) {
        switch (summaryDefinition.getFunction()) {
            case "var":
            case "stdDev": {

                String target = parameterHelper.substituteParameterValues(summaryDefinition.getTarget());
                return Arrays.asList(
                        new CalcColOperator.CalculatedColumn(target + "_squared", "1.0d * " + target + "*" + target)
                );
            }
            default: {
                return null;
            }
        }
    }

    public static List<GroupByNode.SummaryDefinition> getSummariesForPartition(GroupByNode.SummaryDefinition summaryDefinition, ParameterHelper parameterHelper) {
        String target = parameterHelper.substituteParameterValues(summaryDefinition.getTarget());
        switch (summaryDefinition.getFunction()) {
            case "avg": {
                return Arrays.asList(
                        new GroupByNode.SummaryDefinition(target + "_sum", "sum", target, new Object[] {ColumnType.Double})
                );
            }
            case "var":
            case "stdDev": {
                return Arrays.asList(
                        new GroupByNode.SummaryDefinition(target + "_sumsquares", "sum", target + "_squared", new Object[] {ColumnType.Double}),
                        new GroupByNode.SummaryDefinition(target + "_sum", "sum", target, new Object[] {ColumnType.Double})
                );
            }
            case "sum": {
                return Arrays.asList(
                        new GroupByNode.SummaryDefinition(target, "sum", target, summaryDefinition.getArguments())
                );
            }
            default: {
                throw new UnsupportedOperationException("Cannot distribute summary of type " + summaryDefinition.getFunction());
            }
        }
    }

    public static List<IGroupByConfig.Summary> getSummariesForCoalescor(IGroupByConfig.Summary summary, String countColumnName) {
        switch (summary.getFunction()) {
            case "avg": {
                String avgSumName = summary.getTarget() + "_sum";
                return Arrays.asList(
                        new IGroupByConfig.Summary(avgSumName, "sum", avgSumName)
                );
            }
            case "var":
            case "stdDev": {
                return Arrays.asList(
                        new IGroupByConfig.Summary(summary.getTarget() + "_sumsquares", "sum", summary.getTarget() + "_sumsquares"),
                        new IGroupByConfig.Summary(summary.getTarget() + "_sum", "sum", summary.getTarget() + "_sum")
                );
            }
            case "sum": {
                return Arrays.asList(new IGroupByConfig.Summary(summary.getName(), "sum", summary.getTarget()));
            }
            default: {
                throw new UnsupportedOperationException("Cannot distribute summary of type " + summary.getFunction());
            }
        }
    }

    public static List<CalcColOperator.CalculatedColumn> getPostSummaryCalculationsForCoalescor(IGroupByConfig.Summary summary) {
        switch (summary.getFunction()) {
            case "avg": {
                String countName = "count";
                if (summary.getArguments() != null && summary.getArguments().length > 0) {
                    countName = (String) summary.getArguments()[0];
                }
                return Arrays.asList(
                        new CalcColOperator.CalculatedColumn(summary.getName(), summary.getTarget() + "_sum / (" + countName + " * 1.0d)")
                );
            }
            case "var":
            case "stdDev": {
                String countName = "count";
                if (summary.getArguments() != null && summary.getArguments().length > 0) {
                    countName = (String) summary.getArguments()[0];
                }
                countName = "(1.0d * " + countName + ")";
                String expression = "(" + summary.getTarget() + "_sumsquares - (" + summary.getTarget() + "_sum * " + summary.getTarget() + "_sum / " + countName + ")) / " + countName;
                if (summary.getFunction().equals("stdDev")) {
                    expression = "sqrt(" + expression + ")";
                }
                return Arrays.asList(
                        new CalcColOperator.CalculatedColumn(summary.getName(), expression)
                );
            }
            default: {
                return Arrays.asList();
            }
        }
    }

    public static List<String> getExcludeColumns(IGroupByConfig.Summary summary) {
        switch (summary.getFunction()) {
            case "avg": {
                return Arrays.asList(
                        summary.getTarget() + "_sum"
                );
            }
            case "var":
            case "stdDev": {
                return Arrays.asList(
                        summary.getTarget() + "_sumsquares",
                        summary.getTarget() + "_sum"
                );
            }
            default: {
                return Arrays.asList();
            }
        }
    }
}
