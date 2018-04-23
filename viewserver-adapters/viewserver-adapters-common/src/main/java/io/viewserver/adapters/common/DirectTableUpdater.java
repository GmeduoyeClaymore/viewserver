package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.RecordUtils;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.reactor.IReactor;
import rx.Emitter;
import rx.Observable;

public class DirectTableUpdater implements IDatabaseUpdater{
    private final IExecutionContext executionContext;
    private ICatalog serverCatalog;

    public DirectTableUpdater(IExecutionContext executionContext, ICatalog serverCatalog) {
        this.executionContext = executionContext;
        this.serverCatalog = serverCatalog;
    }

    @Override
    public void addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record) {
        if(!Thread.currentThread().getName().startsWith("reactor-")){
            scheduleAddOrUpdateRow(tableName,schemaConfig,record);
        }
        RecordUtils.addRecordToTableOperator((KeyedTable) serverCatalog.getOperator(tableName),record);
    }

    @Override
    public Observable<Boolean> scheduleAddOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record) {
        return Observable.create(subscriber -> this.executionContext.getReactor().scheduleTask(() -> {
            try{
                addOrUpdateRow(tableName,schemaConfig,record);
                subscriber.onNext(true);
                subscriber.onCompleted();
            }catch (Exception ex){
                subscriber.onError(ex);
            }
        },0,0), Emitter.BackpressureMode.BUFFER);
    }
}
