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

package io.viewserver.execution.context;

import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.report.IGraphDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportExecutionPlanContext extends ReportContextExecutionPlanContext {
    private IGraphDefinition graphDefinition;
    private final Map<String, String> calculationAliases = new HashMap<>();
    private final List<CalcColOperator.CalculatedColumn> calculations = new ArrayList<>();

    public IGraphDefinition getGraphDefinition() {
        return graphDefinition;
    }

    public void setGraphDefinition(IGraphDefinition graphDefinition) {
        this.graphDefinition = graphDefinition;
    }

    public Map<String, String> getCalculationAliases() {
        return calculationAliases;
    }

    public List<CalcColOperator.CalculatedColumn> getCalculations() {
        return calculations;
    }
}
