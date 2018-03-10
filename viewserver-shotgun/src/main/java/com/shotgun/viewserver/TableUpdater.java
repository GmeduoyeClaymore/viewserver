package com.shotgun.viewserver;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.RowUpdater;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import rx.Emitter;
import rx.Observable;
import rx.Subscriber;

import java.util.HashMap;

public class TableUpdater {
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

    public int addOrUpdateRow(String tableName, String dataSourceName, IRecord record){
        KeyedTable table = ControllerUtils.getKeyedTable(tableName);
        return addOrUpdateRow(table, dataSourceName, record);
    }

    public int addOrUpdateRow( KeyedTable table, String dataSourceName, IRecord record) {
        if(!Thread.currentThread().getName().startsWith("reactor-")){
            throw new RuntimeException("This code is being called from a non reactor thread this is wrong");
        }
        rowUpdater.setTable(table);
        rowUpdater.setRecord(record);
        rowUpdater.setDataSource(dataSourceRegistry.get(dataSourceName));
        tableUpdater.setTable(table);
        return tableUpdater.addOrUpdateRow(rowUpdater);
    }

    public Observable<Integer> scheduleAddOrUpdateRow(KeyedTable table, String dataSourceName, IRecord record){
        return Observable.create(subscriber -> reactor.scheduleTask(() -> {
            try{
                int rowId  = addOrUpdateRow(table,dataSourceName,record);
                subscriber.onNext((Integer)rowId);
                subscriber.onCompleted();
            }catch (Exception ex){
                subscriber.onError(ex);
            }
        },0,0), Emitter.BackpressureMode.BUFFER);

    }
}
