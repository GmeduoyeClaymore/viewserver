package io.viewserver.adapters.common;

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.RecordUtils;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.KeyedTable;
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
            scheduleAddOrUpdateRow(tableName,schemaConfig,record).subscribe();
            return;
        }
        IOperator operator = serverCatalog.getOperatorByPath(tableName);
        if(operator == null){
            throw new RuntimeException("Unable to find operator named " + tableName);
        }
        RecordUtils.addRecordToTableOperator((KeyedTable) operator,record);
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
