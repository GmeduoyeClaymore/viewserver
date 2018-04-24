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

package io.viewserver.execution;

import io.viewserver.configurator.ConfiguratorSpec;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.steps.CalculationsStep;
import io.viewserver.report.CalculationDefinition;
import io.viewserver.report.ReportDefinition;
import org.junit.Test;

/**
 * Created by bemm on 31/10/2014.
 */
public class CalculationsStepTest {
    @Test
    public void test() throws Exception {
        ReportExecutionPlanContext context = new ReportExecutionPlanContext();

        ReportContext reportContext = new ReportContext();
        context.setReportContext(reportContext);

        ReportDefinition reportDefinition = new ReportDefinition("test", "test");
        reportDefinition.getCalculations().put("calc1", new CalculationDefinition("calc1", "col1+2"));
        reportDefinition.getCalculations().put("calc2", new CalculationDefinition("calc2", "calc1+3"));
        context.setGraphDefinition(reportDefinition);

        ConfiguratorSpec configuratorSpec = new ConfiguratorSpec();
        CalculationsStep step = new CalculationsStep();

        step.execute(context);
    }
}
