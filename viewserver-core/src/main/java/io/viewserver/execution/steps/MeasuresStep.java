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

package io.viewserver.execution.steps;

import io.viewserver.execution.InvalidReportContextException;
import io.viewserver.execution.ReportContext;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.report.CalculationDefinition;
import io.viewserver.report.IMeasuringGraphDefinition;
import io.viewserver.report.MeasureDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bemm on 31/10/2014.
 */
public class MeasuresStep extends CalculationStepBase<IMeasuringGraphDefinition> {
    @Override
    protected List<CalculationDefinition> getCalculations(ReportContext reportContext, IMeasuringGraphDefinition measuringGraphDefinition) {
        ArrayList<CalculationDefinition> calculations = new ArrayList<>();

        ValueLists.IStringList measuresParameter = (ValueLists.IStringList) reportContext.getParameterValues().get("measures");
        if (measuresParameter != null && !measuresParameter.isEmpty()) {
            int size = measuresParameter.size();
            for (int i = 0; i < size; i++) {
                String measureName = measuresParameter.get(i);
                Map<String, MeasureDefinition> measures = measuringGraphDefinition.getMeasures();
                MeasureDefinition measureDefinition = measures.get((String) measureName);
                if(measureDefinition == null){
                    throw new InvalidReportContextException(String.format("Unable to find measure \"%s\". Supported measures are \"%s\"",measureName,String.join(",",measures.keySet())));
                }
                for (CalculationDefinition calculationDefinition : measureDefinition.getPreSummaryCalculations()) {
                    calculations.add(new CalculationDefinition(calculationDefinition.getName(), calculationDefinition.getExpression()));
                }
            }
        }

        return calculations;
    }
}
