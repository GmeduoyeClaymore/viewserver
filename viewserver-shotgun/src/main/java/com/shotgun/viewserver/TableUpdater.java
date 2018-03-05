package com.shotgun.viewserver;

import io.viewserver.adapters.common.RowUpdater;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.KeyedTable;

public class TableUpdater {
    private final RowUpdater rowUpdater;
    private final LocalKeyedTableUpdater tableUpdater;
    private final IDataSourceRegistry<DataSource> dataSourceRegistry;

    public TableUpdater(IExecutionContext executionContext, DimensionMapper dimensionMapper, IDataSourceRegistry<DataSource> dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
        tableUpdater = new LocalKeyedTableUpdater(executionContext, null);
        rowUpdater = new RowUpdater(dimensionMapper, executionContext.getFunctionRegistry(), executionContext.getExpressionParser());
    }

    public void addOrUpdateRow(String tableName, String dataSourceName, IRecord record){
        KeyedTable table = ControllerUtils.getKeyedTable(tableName);
        addOrUpdateRow(table, dataSourceName, record);
    }

    public void addOrUpdateRow( KeyedTable table, String dataSourceName, IRecord record) {
        rowUpdater.setTable(table);
        rowUpdater.setRecord(record);
        rowUpdater.setDataSource(dataSourceRegistry.get(dataSourceName));
        tableUpdater.setTable(table);
        tableUpdater.addOrUpdateRow(rowUpdater);
    }
}
