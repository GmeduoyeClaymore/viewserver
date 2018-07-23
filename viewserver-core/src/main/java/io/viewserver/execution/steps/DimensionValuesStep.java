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

import io.viewserver.core.NullableBool;
import io.viewserver.datasource.*;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.DimensionExecutionPlanContext;
import io.viewserver.execution.nodes.IndexOutputNode;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.operators.index.IndexOperator;
import io.viewserver.operators.index.QueryHolderConfig;

//TODO - this is currently the same as the dimensions step but I anticipate they will diverge
public class DimensionValuesStep implements IExecutionPlanStep<DimensionExecutionPlanContext>{

    private DimensionMapper dimensionMapper;

    public DimensionValuesStep(DimensionMapper dimensionMapper) {
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public void execute(DimensionExecutionPlanContext dimensionExecutionPlanContext) {
        ReportContext reportContext = dimensionExecutionPlanContext.getReportContext();
        IDataSource dataSource = dimensionExecutionPlanContext.getDataSource();

        if (reportContext.getDimensionValues().size() == 0) {
            return;
        }

        final QueryHolderConfig[] queryHolders = new QueryHolderConfig[reportContext.getDimensionValues().size()];
        int i = 0;

        for (ReportContext.DimensionValue dimensionFilter : reportContext.getDimensionValues()) {
            Dimension dimension = dataSource.getDimension(dimensionFilter.getName());
            queryHolders[i++] = new QueryHolderConfig(dimension,dimensionFilter.isExclude(), dimensionFilter.getValues().toArray() );
        }

        IndexOutputNode indexNode = new IndexOutputNode(IDataSourceRegistry.getOperatorPath(dataSource, DataSource.INDEX_NAME))
                .withDataSourceName(dataSource.getName())
                .withQueryHolders(queryHolders);

        dimensionExecutionPlanContext.addNodes(indexNode);
        dimensionExecutionPlanContext.setInput(indexNode.getName(), indexNode.getConfigForOutputName());
    }
}
