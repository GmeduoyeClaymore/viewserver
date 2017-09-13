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

import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.datasource.IDataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.IGraphNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by paulg on 31/10/2014.
 */
public class DataSourceStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    private static final Logger log = LoggerFactory.getLogger(DataSourceStep.class);

    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();

        for (IGraphNode node : dataSource.getNodes()) {
            for (IConfiguratorSpec.Connection connection : node.getConnections()) {
                if (connection.getOperator().equals("#input")) {
                    connection.setOperator(dataSourceExecutionPlanContext.getInputOperator());
                    connection.setOutput(dataSourceExecutionPlanContext.getInputOutputName());
                }
            }
        }
        dataSourceExecutionPlanContext.addNodes(dataSource.getNodes());

        if (dataSource.getOutput() == null) {
            log.warn("Data source '{}' has no output set - results may be unpredictable!", dataSource);
        } else {
            dataSource.setFinalOutput(IDataSourceRegistry.getOperatorPath(dataSource, dataSource.getOutput()));
        }
    }
}
