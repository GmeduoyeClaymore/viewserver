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
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.execution.plan.DataSourceExecutionPlan;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnMetadata;

import java.util.List;

/**
 * Created by nickc on 21/11/2014.
 */
public class DataSourceHelper {

    public static io.viewserver.schema.Schema getSchema(SchemaConfig schema1 ) {
        io.viewserver.schema.Schema schema = new io.viewserver.schema.Schema();
        List<Column> columns = schema1.getColumns();
        int count = columns.size();
        for (int i = 0; i < count; i++) {
            Column column = columns.get(i);
            ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder(column.getName(), column.getType().getColumnType());
            schema.addColumn(columnHolder);
        }
        return schema;
    }
    public static void runDataSourceExecutionPlan(IDataSource dataSource, IDataSourceRegistry dataSourceRegistry, IExecutionContext executionContext, ICatalog catalog, CommandResult commandResult) {
        ExecutionPlanRunner executionPlanRunner = new ExecutionPlanRunner();
        DataSourceExecutionPlanContext dataSourceExecutionPlanContext = new DataSourceExecutionPlanContext(dataSource);
        dataSourceExecutionPlanContext.setExecutionContext(executionContext);
        dataSourceExecutionPlanContext.setCatalog(catalog);
        DataSourceExecutionPlan dataSourceExecutionPlan = new DataSourceExecutionPlan();

        executionPlanRunner.executePlan(dataSourceExecutionPlan, dataSourceExecutionPlanContext, executionContext, catalog, commandResult);

        dataSourceRegistry.onDataSourceBuilt(dataSourceExecutionPlanContext);
    }


    public static ColumnHolder createColumnHolder(String dimensionNamespace, String dimensionName, ColumnType type,Cardinality cardinality,IDimensionMapper dimensionMapper) {
        io.viewserver.schema.column.ColumnType schemaColumnType = type.getColumnType();
        if (dimensionMapper != null) {
            dimensionMapper.registerDimension(dimensionNamespace, dimensionName, type);
        }
        ColumnHolder columnHolder = ColumnHolderUtils.createColumnHolder(dimensionName, schemaColumnType);
        ColumnMetadata columnMetadata = ColumnHolderUtils.createColumnMetadata(schemaColumnType);
        columnMetadata.setDataType(type);
        columnMetadata.setFlag(ColumnFlags.DIMENSION);
        columnMetadata.setDimensionNameSpace(dimensionNamespace);
        columnMetadata.setDimensionName(dimensionName);
        columnMetadata.setCardinality(cardinality);
        columnHolder.setMetadata(columnMetadata);
        return columnHolder;
    }

    public static TableKeyDefinition getTableKeyDefinition(DataSource dataSource) {
        List<String> keyColumns = dataSource.getSchema().getKeyColumns();
        if (keyColumns == null || keyColumns.isEmpty()) {
            throw new IllegalArgumentException("Cannot get table key definition from datasource - no key columns are provided");
        }
        return new TableKeyDefinition(keyColumns.toArray(new String[keyColumns.size()]));
    }
}
