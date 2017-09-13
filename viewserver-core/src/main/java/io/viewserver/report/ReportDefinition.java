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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 31/10/2014.
 */
public class ReportDefinition extends GraphDefinitionBase<ReportDefinition>
        implements IParameterisedGraphDefinition, ICalculatingGraphDefinition, IMeasuringGraphDefinition {
    private static final Logger log = LoggerFactory.getLogger(ReportDefinition.class);
    private String id;
    private String name;
    private final Map<String, ParameterDefinition> parameters = new LinkedHashMap<>();
    private final Map<String, CalculationDefinition> calculations = new LinkedHashMap<>();
    private Map<String, MeasureDefinition> measures = new LinkedHashMap<>();
    private String dataSource;

    public ReportDefinition() {
    }

    public ReportDefinition(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Map<String, ParameterDefinition> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, CalculationDefinition> getCalculations() {
        return calculations;
    }

    @Override
    public Map<String, MeasureDefinition> getMeasures() {
        return measures;
    }

    public ReportDefinition withMeasures(Map<String, MeasureDefinition> measures) {
        this.measures = measures;
        return this;
    }

    public ReportDefinition withParameter(String name, String label, Class type, Object... validValues) {
        parameters.put(name, new ParameterDefinition(name, label, type, validValues));
        return this;
    }

    public ReportDefinition withParameters(ParameterDefinition... parameters) {
        for (ParameterDefinition parameter : parameters) {
            this.parameters.put(parameter.getName(), parameter);
        }
        return this;
    }

    public ReportDefinition withCalculation(String name, String expression) {
        calculations.put(name, new CalculationDefinition(name, expression));
        return this;
    }

    public ReportDefinition withCalculations(CalculationDefinition... calculations) {
        for (CalculationDefinition calculation : calculations) {
            this.calculations.put(calculation.getName(), calculation);
        }
        return this;
    }

    public ReportDefinition withCalculations(List<CalculationDefinition> calculations) {
        for (CalculationDefinition calculation : calculations) {
            this.calculations.put(calculation.getName(), calculation);
        }
        return this;
    }

    public ReportDefinition disableMeasures(List<String> disabledMeasures) {
        for (String disabledMeasure : disabledMeasures) {
            measures.remove(disabledMeasure);
        }

        return this;
    }

    public ReportDefinition withMeasure(String name, String label, List<CalculationDefinition> preSummaryCalculations,
                                        List<GroupByNode.SummaryDefinition> summaries,
                                        List<CalculationDefinition> postSummaryCalculations) {
        measures.put(name,
                new MeasureDefinition(name, label)
                        .withPreSummaryCalculations(preSummaryCalculations)
                        .withSummaries(summaries)
                        .withPostSummaryCalculations(postSummaryCalculations)
        );
        return this;
    }

    public ReportDefinition withMeasures(MeasureDefinition... measures) {
        for (MeasureDefinition measure : measures) {
            this.measures.put(measure.getName(), measure);
        }
        return this;
    }

    public String getDataSource() {
        return dataSource;
    }

    public ReportDefinition withDataSource(String dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
