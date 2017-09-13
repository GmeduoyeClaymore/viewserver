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

import io.viewserver.Constants;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by nickc on 01/11/2014.
 */
public class CalculationProjectionStep implements IExecutionPlanStep<ReportExecutionPlanContext> {
    private final Map<String, IProjectionConfig.ProjectionColumn> projectionColumns = new HashMap<>();

    @Override
    public void execute(final ReportExecutionPlanContext reportExecutionPlanContext) {
        IDataSource dataSourceDefinition = reportExecutionPlanContext.getDataSource();
        List<CalcColOperator.CalculatedColumn> calculations = reportExecutionPlanContext.getCalculations();
        if (calculations.size() == 0) {
            return;
        }

        String calcsName = IDataSourceRegistry.getOperatorPath(dataSourceDefinition, DataSource.CALCS_NAME);
        CalcColNode calcColNode = new CalcColNode(calcsName)
                .withCalculations(calculations)
                .withColumnAliases(reportExecutionPlanContext.getCalculationAliases())
                .withDataRefreshedOnColumnAdd(false)
                .withDistribution();

        projectionColumns.clear();
        reportExecutionPlanContext.getCalculationAliases().entrySet().forEach(addProjectionColumnProc);

        ProjectionNode projectionNode = new ProjectionNode("__projection")
                .withMode(IProjectionConfig.ProjectionMode.Projection)
                .withProjectionColumns(projectionColumns.values())
                .withConnection(reportExecutionPlanContext.getInputOperator(), reportExecutionPlanContext.getInputOutputName(), Constants.IN)
                .withDistribution();

        reportExecutionPlanContext.addNodes(calcColNode, projectionNode);
        reportExecutionPlanContext.setInput(projectionNode.getName());
    }

    private final Consumer<? super Map.Entry<String, String>> addProjectionColumnProc = entry -> {
        String inboundName = entry.getValue();
        String outboundName = entry.getKey();
        IProjectionConfig.ProjectionColumn projectionColumn = projectionColumns.get(inboundName);
        if (projectionColumn != null) {
            projectionColumn.addOutboundName(outboundName);
        } else {
            projectionColumn = new IProjectionConfig.ProjectionColumn(inboundName, outboundName);
            projectionColumns.put(inboundName, projectionColumn);
        }
    };
}
