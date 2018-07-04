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

package io.viewserver.execution.plan;

import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.steps.IExecutionPlanStep;
import io.viewserver.operators.calccol.CalcColOperator;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bemm on 23/10/15.
 */
public class DataSourceCalculationsStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();
        EnumSet<DataSourceOption> options = dataSource.getOptions();
        List<CalculatedColumn> calculatedColumns = dataSource.getCalculatedColumns();
        List<IGraphNode> nodes = dataSourceExecutionPlanContext.getGraphNodes();

        if (options.contains(DataSourceOption.HasCalculations)
                || options.contains(DataSourceOption.IsReportSource)
                || !calculatedColumns.isEmpty()) {
            nodes.add(new CalcColNode(DataSource.CALCS_NAME)
                    .withCalculations(calculatedColumns.stream().map(col -> new CalcColOperator.CalculatedColumn(col.getName(),col.getExpression())).collect(Collectors.toList()))
                    .withDataRefreshedOnColumnAdd(false)
                    .withConnection(dataSource.getFinalOutput(), Constants.OUT, Constants.IN));
            // set the output of the data source to be the calculations
            // if a report context specifies no index filters, then this will be the report source
            // otherwise, the output from the index filter will be the report source
            dataSource.setFinalOutput(IDataSourceRegistry.getOperatorPath(dataSource, DataSource.CALCS_NAME));
        }
    }
}


