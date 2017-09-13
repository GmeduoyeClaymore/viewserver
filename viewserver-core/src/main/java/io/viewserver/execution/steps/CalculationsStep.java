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

import io.viewserver.execution.ReportContext;
import io.viewserver.report.CalculationDefinition;
import io.viewserver.report.ICalculatingGraphDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by nickc on 31/10/2014.
 */
public class CalculationsStep extends CalculationStepBase<ICalculatingGraphDefinition> {
    private final List<CalculationDefinition> calculations = new ArrayList<>();

    @Override
    protected List<CalculationDefinition> getCalculations(ReportContext reportContext, ICalculatingGraphDefinition calculatingGraphDefinition) {
        calculations.clear();
        calculatingGraphDefinition.getCalculations().entrySet().forEach(addCalculationProc);
        return calculations;
    }

    private final Consumer<Map.Entry<String, CalculationDefinition>> addCalculationProc = entry -> {
        calculations.add(entry.getValue());
    };
}
