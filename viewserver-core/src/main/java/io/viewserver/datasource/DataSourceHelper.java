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
import io.viewserver.execution.ExecutionPlanRunner;
import io.viewserver.execution.IExecutionPlanRunner;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.plan.DataSourceExecutionPlan;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by nickc on 21/11/2014.
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


}
