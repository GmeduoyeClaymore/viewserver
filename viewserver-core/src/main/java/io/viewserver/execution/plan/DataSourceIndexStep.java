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
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.DataSourceOption;
import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.IDataSource;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.execution.nodes.IndexNode;
import io.viewserver.execution.steps.IExecutionPlanStep;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bemm on 23/10/15.
 */
public class DataSourceIndexStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();
        EnumSet<DataSourceOption> options = dataSource.getOptions();
        List<IGraphNode> nodes = dataSourceExecutionPlanContext.getGraphNodes();
        List<Dimension> dimensions = dataSource.getDimensions();

        if (options.contains(DataSourceOption.IsIndexed)
                || !dimensions.isEmpty()) {
            nodes.add(new IndexNode(DataSource.INDEX_NAME)
                    .withDatasourceName(dataSource.getName())
                    .withIndexedColumns(dimensions.stream().map(x -> x.getName()).collect(Collectors.toList()))
                    .withConnection(dataSource.getFinalOutput(), Constants.OUT, Constants.IN));
        }
    }
}
