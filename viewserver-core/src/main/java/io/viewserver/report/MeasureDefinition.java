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

package io.viewserver.report;

import io.viewserver.execution.nodes.GroupByNode;

import java.util.ArrayList;
import java.util.List;

public class MeasureDefinition {
    private String name;
    private String label;
    private final List<CalculationDefinition> preSummaryCalculations = new ArrayList<>();
    private final List<GroupByNode.SummaryDefinition> summaries = new ArrayList<>();
    private final List<CalculationDefinition> postSummaryCalculations = new ArrayList<>();

    public MeasureDefinition(){}

    public MeasureDefinition(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label != null ? label : name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<CalculationDefinition> getPreSummaryCalculations() {
        return preSummaryCalculations;
    }

    public List<GroupByNode.SummaryDefinition> getSummaries() {
        return summaries;
    }

    public List<CalculationDefinition> getPostSummaryCalculations() {
        return postSummaryCalculations;
    }

    public MeasureDefinition withPreSummaryCalculation(String name, String expression) {
        preSummaryCalculations.add(new CalculationDefinition(name, expression));
        return this;
    }

    public MeasureDefinition withPreSummaryCalculations(List<CalculationDefinition> calculations) {
        this.preSummaryCalculations.addAll(calculations);
        return this;
    }

    public MeasureDefinition withSummary(String name, String function, String target) {
        this.summaries.add(new GroupByNode.SummaryDefinition(name, function, target));
        return this;
    }

    public MeasureDefinition withSummary(String name, String function, String target, Object[] arguments) {
        this.summaries.add(new GroupByNode.SummaryDefinition(name, function, target, arguments));
        return this;
    }

    public MeasureDefinition withRegexSummary(String name, String function, String target) {
        this.summaries.add(new GroupByNode.SummaryDefinition(name, function, target, true));
        return this;
    }

    public MeasureDefinition withRegexSummary(String name, String function, String target, Object[] arguments) {
        this.summaries.add(new GroupByNode.SummaryDefinition(name, function, target, true, arguments));
        return this;
    }

    public MeasureDefinition withSummaries(List<GroupByNode.SummaryDefinition> summaries) {
        this.summaries.addAll(summaries);
        return this;
    }

    public MeasureDefinition withPostSummaryCalculation(String name, String expression) {
        postSummaryCalculations.add(new CalculationDefinition(name, expression));
        return this;
    }

    public MeasureDefinition withPostSummaryCalculations(List<CalculationDefinition> calculations) {
        this.postSummaryCalculations.addAll(calculations);
        return this;
    }
}
