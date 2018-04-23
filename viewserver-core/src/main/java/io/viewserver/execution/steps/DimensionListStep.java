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

import io.viewserver.datasource.Dimension;
import io.viewserver.datasource.IDataSource;
import io.viewserver.execution.context.DataSourceExecutionPlanContext;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

/**
 * Created by paulg on 31/10/2014.
 */
public class DimensionListStep implements IExecutionPlanStep<DataSourceExecutionPlanContext> {
    @Override
    public void execute(DataSourceExecutionPlanContext dataSourceExecutionPlanContext) {
        IDataSource dataSource = dataSourceExecutionPlanContext.getDataSource();
        buildDimensionListTable(dataSourceExecutionPlanContext, dataSource);
    }

    private void buildDimensionListTable(DataSourceExecutionPlanContext dataSourceExecutionPlanContext, IDataSource dataSource) {
        //Build the dimensions list table
        Table table = new Table("dimensions", dataSourceExecutionPlanContext.getExecutionContext(), dataSourceExecutionPlanContext.getCatalog(),
                getDimensionListSchema(), new ChunkedColumnStorage(1024));
        table.register();
        table.initialise(1024);

        for (Dimension dimension : dataSource.getDimensions()) {
            ITableRowUpdater rowUpdater = new ITableRowUpdater() {
                @Override
                public void setValues(ITableRow row) {
                    row.setString("label", dimension.getLabel());
                    row.setString("name", dimension.getName());
                    row.setString("group", dimension.getGroup());
                    row.setString("plural", dimension.getPlural() != null ? dimension.getPlural() : dimension.getName());
                }
            };
            table.addRow(rowUpdater);
        }
    }

    private Schema getDimensionListSchema() {
        Schema schema = new Schema();
        schema.addColumn(ColumnHolderUtils.createColumnHolder("label", io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder("name", io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder("group", io.viewserver.schema.column.ColumnType.String));
        schema.addColumn(ColumnHolderUtils.createColumnHolder("plural", io.viewserver.schema.column.ColumnType.String));

        return schema;
    }
}
