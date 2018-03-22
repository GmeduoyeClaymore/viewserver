package com.shotgun.viewserver;

import io.viewserver.adapters.common.RowUpdater;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.reactor.IReactor;
import rx.Emitter;
import rx.Observable;

public class TableUpdater implements IDatabaseUpdater{
    private final RowUpdater rowUpdater;
    private final LocalKeyedTableUpdater tableUpdater;
    private final IDataSourceRegistry<DataSource> dataSourceRegistry;
    private final IReactor reactor;

    public TableUpdater(IExecutionContext executionContext, DimensionMapper dimensionMapper, IDataSourceRegistry<DataSource> dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
        this.reactor = executionContext.getReactor();
        tableUpdater = new LocalKeyedTableUpdater(executionContext, null);
        rowUpdater = new RowUpdater(dimensionMapper, executionContext.getFunctionRegistry(), executionContext.getExpressionParser());
    }

    public void addOrUpdateRow(String tableName, String dataSourceName, IRecord record){
        KeyedTable table = ControllerUtils.getKeyedTable(tableName);
         addOrUpdateRow(table, dataSourceName, record);
    }

    public void addOrUpdateRow( KeyedTable table, String dataSourceName, IRecord record) {
        if(!Thread.currentThread().getName().startsWith("reactor-")){
            throw new RuntimeException("This code is being called from a non reactor thread this is wrong");
        }
        rowUpdater.setTable(table);
        rowUpdater.setRecord(record);
        rowUpdater.setDataSource(dataSourceRegistry.get(dataSourceName));
        tableUpdater.setTable(table);
        tableUpdater.addOrUpdateRow(rowUpdater);
    }

    public Observable<Boolean> scheduleAddOrUpdateRow(KeyedTable table, String dataSourceName, IRecord record){
        return Observable.create(subscriber -> reactor.scheduleTask(() -> {
            try{
                addOrUpdateRow(table,dataSourceName,record);
                subscriber.onNext(true);
                subscriber.onCompleted();
            }catch (Exception ex){
                subscriber.onError(ex);
            }
        },0,0), Emitter.BackpressureMode.BUFFER);

    }
}

