package com.shotgun.viewserver;

import io.viewserver.adapters.common.RowUpdater;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.KeyedTable;

public class ShotgunTableUpdater{
    private final RowUpdater rowUpdater;
    private final LocalKeyedTableUpdater tableUpdater;
    private final IDataSourceRegistry<DataSource> dataSourceRegistry;

    public ShotgunTableUpdater(IExecutionContext executionContext, DimensionMapper dimensionMapper, IDataSourceRegistry<DataSource> dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
        tableUpdater = new LocalKeyedTableUpdater(executionContext, null);
        rowUpdater = new RowUpdater(dimensionMapper, executionContext.getFunctionRegistry(), executionContext.getExpressionParser());
    }

    public void addOrUpdateRow(String tableName, String dataSourceName, IRecord record){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(tableName);
        rowUpdater.setTable(orderTable);
        rowUpdater.setRecord(record);
        rowUpdater.setDataSource(dataSourceRegistry.get(dataSourceName));
        tableUpdater.setTable(orderTable);
        tableUpdater.addOrUpdateRow(rowUpdater);
    }
}
