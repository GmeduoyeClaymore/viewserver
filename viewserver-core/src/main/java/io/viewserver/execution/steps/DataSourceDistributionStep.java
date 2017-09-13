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

import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.DistributionMode;
import io.viewserver.datasource.IDataSource;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.nodes.DistributionNode;
import io.viewserver.messages.command.IInitialiseSlaveCommand;

/**
 * Created by nickc on 09/12/2014.
 */
public class DataSourceDistributionStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        if (!dataSourceExecutionPlanContext.getDistributionManager().getNodeType()
                .equals(IInitialiseSlaveCommand.Type.Master)) {
            return;
        }

        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();
        DistributionMode distributionMode = dataSource.getDistributionMode();
        if (!distributionMode.equals(DistributionMode.Local)) {
            dataSourceExecutionPlanContext.addNodes(
                    new DistributionNode("distribution")
                            .withMode(distributionMode)
                            .withStripingStrategy(((DataSource) dataSource).getStripingStrategy())
                            .withConnection(dataSource.getName())
            );
        }
    }
}
