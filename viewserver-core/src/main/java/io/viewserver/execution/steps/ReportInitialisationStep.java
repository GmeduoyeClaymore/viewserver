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

import io.viewserver.execution.IParameterHolder;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.messages.command.IReportContext;
import io.viewserver.report.DefaultDimensionValues;
import io.viewserver.report.IGraphDefinition;
import io.viewserver.report.ReportDefinition;
import io.viewserver.report.ReportRegistry;

import java.util.List;

/**
 * Created by bemm on 03/12/2014.
 */
public class ReportInitialisationStep implements IExecutionPlanStep<ReportExecutionPlanContext> {
    private ReportRegistry reportRegistry;

    public ReportInitialisationStep(ReportRegistry reportRegistry) {
        this.reportRegistry = reportRegistry;
    }

    @Override
    public void execute(ReportExecutionPlanContext reportExecutionPlanContext) {
        ReportContext reportContext = reportExecutionPlanContext.getReportContext();
        ReportDefinition definition = reportRegistry.getReportById(reportContext.getReportName());
        reportExecutionPlanContext.setParameterHelper(new ParameterHelper(definition, reportContext));

        IGraphDefinition reportDefinition = reportExecutionPlanContext.getGraphDefinition();
        final List<IGraphNode> graphNodes = reportDefinition.getNodes();
    }

}
