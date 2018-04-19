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

        final IndexOperator.QueryHolder[] queryHolders = new IndexOperator.QueryHolder[reportContext.getDimensionValues().size()];
        int i = 0;
        for (ReportContext.DimensionValue dimensionFilter : reportContext.getDimensionValues()) {
            Dimension dimension = dataSource.getDimension(dimensionFilter.getName());

            final ValueLists.IValueList values = dimensionFilter.getValues();
            int[] mappedValues = new int[values.size()];
            for (int j = 0; j < mappedValues.length; j++) {
                final Object value;
                if (values instanceof ValueLists.IBooleanList) {
                    if (dimension.getContentType() == ContentType.NullableBool) {
                        value = NullableBool.fromBoolean(((ValueLists.IBooleanList) values).get(j));
                        // commenting out the following, as I can't see a path that gets us to here - the report context
                        // has no capability for dealing with nullable booleans as it stands
//                    } else if (value == null) {
//                        value = NullableBool.Null;
                    } else {
                        value = ((ValueLists.IBooleanList) values).get(j);
                    }
                } else if (values instanceof ValueLists.IIntegerList) {
                    value = ((ValueLists.IIntegerList)values).get(j);
                } else if (values instanceof ValueLists.ILongList) {
                    value = ((ValueLists.ILongList)values).get(j);
                } else if (values instanceof ValueLists.IFloatList) {
                    value = ((ValueLists.IFloatList)values).get(j);
                } else if (values instanceof ValueLists.IDoubleList) {
                    value = ((ValueLists.IDoubleList)values).get(j);
                } else if (values instanceof ValueLists.IStringList) {
                    value = ((ValueLists.IStringList)values).get(j);
                } else {
                    throw new UnsupportedOperationException(String.format("Unsupported type of value list - %s", values.getClass().getName()));
                }
                mappedValues[j] = dimensionMapper.map(dataSource.getName(), dimension.getName(), dimension.getContentType(), value);
            }

            IndexOperator.QueryHolder queryHolder = dimensionFilter.isExclude()
                    ? IndexOperator.QueryHolder.exclude(dimensionFilter.getName(), mappedValues)
                    : IndexOperator.QueryHolder.include(dimensionFilter.getName(), mappedValues);
            queryHolders[i++] = queryHolder;
        }

        IndexOutputNode indexNode = new IndexOutputNode(IDataSourceRegistry.getDefaultOperatorPath(dataSource, DataSource.INDEX_NAME))
                .withQueryHolders(queryHolders);

        dimensionExecutionPlanContext.addNodes(indexNode);
        dimensionExecutionPlanContext.setInput(indexNode.getName(), indexNode.getConfigForOutputName());
    }
}
