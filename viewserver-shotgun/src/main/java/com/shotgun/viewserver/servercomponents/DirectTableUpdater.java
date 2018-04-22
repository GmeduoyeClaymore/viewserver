package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.RecordUtils;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.reactor.IReactor;
import rx.Emitter;
import rx.Observable;

public class DirectTableUpdater implements IDatabaseUpdater{
    private final IExecutionContext executionContext;

    public DirectTableUpdater(IExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void addOrUpdateRow(String tableName, String dataSourceName, IRecord record){
        KeyedTable table = ControllerUtils.getKeyedTable(tableName);
        addOrUpdateRow(table, dataSourceName, record);
    }

    public void addOrUpdateRow( KeyedTable table, String dataSourceName, IRecord record) {
        if(!Thread.currentThread().getName().startsWith("reactor-")){
            throw new RuntimeException("This code is being called from a non reactor thread this is wrong");
        }
        RecordUtils.addRecordToTableOperator(table,record);
    }

    public Observable<Boolean> scheduleAddOrUpdateRow(KeyedTable table, String dataSourceName, IRecord record){
        return Observable.create(subscriber -> this.executionContext.getReactor().scheduleTask(() -> {
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
