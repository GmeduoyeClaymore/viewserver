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

package io.viewserver.datasource;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.NullableBool;
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.execution.InvalidReportContextException;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.plan.DataSourceExecutionPlan;
import io.viewserver.messages.common.ValueLists;
import io.viewserver.operators.index.IndexOperator;
import io.viewserver.operators.index.QueryHolderConfig;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by bemm on 21/11/2014.
 */
public class DataSourceHelper {

    private static final Logger log = LoggerFactory.getLogger(DataSourceExecutionPlan.class);

    public static void runDataSourceExecutionPlan(IExecutionPlanRunner executionPlanRunner,IDataSource dataSource, IDataSourceRegistry dataSourceRegistry, IExecutionContext executionContext, ICatalog catalog, CommandResult commandResult) {

        log.info("Starting to run execution plan for " + dataSource.getName());

        DataSourceExecutionPlanContext dataSourceExecutionPlanContext = new DataSourceExecutionPlanContext(dataSource);
        dataSourceExecutionPlanContext.setExecutionContext(executionContext);
        dataSourceExecutionPlanContext.setCatalog(catalog);
        DataSourceExecutionPlan dataSourceExecutionPlan = new DataSourceExecutionPlan();

        executionPlanRunner.executePlan(dataSourceExecutionPlan, dataSourceExecutionPlanContext, executionContext, catalog, commandResult);

        dataSourceRegistry.onDataSourceBuilt(dataSourceExecutionPlanContext);

        if(!commandResult.isSuccess()){
            throw new RuntimeException("Problem running data source execution plan");
        }

        log.info("Finished execution plan for " + dataSource.getName());
    }


    public static ColumnHolder createColumnHolder(String dimensionNamespace, String dimensionName, ContentType type, Cardinality cardinality, IDimensionMapper dimensionMapper) {
        io.viewserver.schema.column.ColumnType schemaColumnType = type.getColumnType();
        if (dimensionMapper != null) {
            dimensionMapper.registerDimension(dimensionNamespace, dimensionName, type);
        }
        ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder(dimensionName, cardinality.getColumnType());
        ColumnMetadata columnMetadata = ColumnHolderUtils.createColumnMetadata(cardinality.getColumnType());
        columnMetadata.setDataType(type);
        columnMetadata.setFlag(ColumnFlags.DIMENSION);
        columnMetadata.setDimensionNameSpace(dimensionNamespace);
        columnMetadata.setDimensionName(dimensionName);
        columnMetadata.setCardinality(cardinality);
        columnHolder.setMetadata(columnMetadata);
        return columnHolder;
    }

    public static QueryHolderConfig[] getQueryHolders(IDataSource dataSource, List<ReportContext.DimensionValue> dimensionValues, DimensionMapper dimensionMapper) {
        final QueryHolderConfig[] queryHolders = new QueryHolderConfig[dimensionValues.size()];
        int i = 0;
        for (ReportContext.DimensionValue dimensionFilter : dimensionValues) {
            //TODO - handle case where dimension does not exist in the data source
            Dimension dimension = dataSource.getDimension(dimensionFilter.getName());
            if(dimension == null){
                throw new RuntimeException("Unable to find dimension named " + dimensionFilter.getName() + " in data source " + dataSource.getName());
            }

            QueryHolderConfig queryHolder = new QueryHolderConfig(dimension,dimensionFilter.isExclude(), dimensionFilter.getValues().toArray());
            queryHolders[i++] = queryHolder;
        }
        return queryHolders;
    }

    public static IndexOperator.QueryHolder getQueryHolder(String dimensionNameSpace, DimensionMapper dimensionMapper, ReportContext.DimensionValue dimensionFilter, Dimension dimension) {
        if(dimension == null){
            throw new InvalidReportContextException(String.format("Could not find the dimension %s in the data source", dimensionFilter.getName()));
        }

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
            mappedValues[j] = dimensionMapper.map(dimension.isGlobal() ? "global" : dimensionNameSpace, dimension.getName(),dimension.getContentType(), value);
        }

        return dimensionFilter.isExclude()
                ? IndexOperator.QueryHolder.exclude(dimensionFilter.getName(), mappedValues)
                : IndexOperator.QueryHolder.include(dimensionFilter.getName(), mappedValues);
    }


}
