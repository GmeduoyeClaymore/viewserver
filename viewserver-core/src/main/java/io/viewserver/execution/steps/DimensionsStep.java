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
import io.viewserver.execution.InvalidReportContextException;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.ReportExecutionPlanContext;
import io.viewserver.execution.nodes.IndexOutputNode;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.operators.index.IndexOperator;
import io.viewserver.operators.index.QueryHolderConfig;

import java.util.List;

import static io.viewserver.datasource.DataSourceHelper.getQueryHolders;

public class DimensionsStep implements IExecutionPlanStep<ReportExecutionPlanContext> {
    private DimensionMapper dimensionMapper;

    public DimensionsStep(DimensionMapper dimensionMapper) {
        this.dimensionMapper = dimensionMapper;
    }

    @Override
    public void execute(ReportExecutionPlanContext reportExecutionPlanContext) {
        ReportContext reportContext = reportExecutionPlanContext.getReportContext();
        IDataSource dataSource = reportExecutionPlanContext.getDataSource();

        List<ReportContext.DimensionValue> dimensionValues = reportContext.getDimensionValues();
        if (dimensionValues.size() == 0) {
            return;
        }

        final QueryHolderConfig[] queryHolders = getQueryHolders(dataSource, dimensionValues, this.dimensionMapper);

        IndexOutputNode indexNode = new IndexOutputNode(IDataSourceRegistry.getOperatorPath(dataSource, DataSource.INDEX_NAME))
                .withDataSourceName(dataSource.getName())
                .withQueryHolders(queryHolders);


        reportExecutionPlanContext.addNodes(indexNode);
        reportExecutionPlanContext.setInput(indexNode.getName(), reportExecutionPlanContext.getParameterHelper().substituteParameterValues(indexNode.getConfigForOutputName()));
    }


}
