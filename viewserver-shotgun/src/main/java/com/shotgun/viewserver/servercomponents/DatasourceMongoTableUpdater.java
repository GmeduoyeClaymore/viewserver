package com.shotgun.viewserver.servercomponents;

import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoTableUpdater;
import io.viewserver.catalog.ICatalog;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.RecordUtils;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.viewserver.core.Utils.toArray;

public class DatasourceMongoTableUpdater extends MongoTableUpdater {
    public static Executor MongoPersistenceExecutor = Executors.newFixedThreadPool(5,new NamedThreadFactory("mongo-persistence"));
    private static final Logger logger = LoggerFactory.getLogger(DatasourceMongoTableUpdater.class);
    private ICatalog catalog;

    public DatasourceMongoTableUpdater(MongoConnectionFactory connectionFactory, ICatalog catalog) {
        super(connectionFactory);
        this.catalog = catalog;
    }

    @Override
    public Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        KeyedTable table = (KeyedTable) catalog.getOperatorByPath(tableName);
        TableKeyDefinition definition = schemaConfig.getTableKeyDefinition();
        TableKey tableKey = RecordUtils.getTableKey(record, definition);
        DataSourceTableName dsTableName = new DataSourceTableName(tableName);
        if(logger.isDebugEnabled()) {
            logger.debug("Updatating {} with record {}", table, record.asString());
        }
        return super.addOrUpdateRow(dsTableName.getDataSourceName(), schemaConfig, record).observeOn(Schedulers.from(MongoPersistenceExecutor)).flatMap(res -> {
            if(!res){
                throw new RuntimeException("Update to record " + tableKey + " has not been acknowleged");
            }
            return waitForRecordUpdate(tableKey,table, (Integer)record.getValue("version"));
        });
    }

    private Observable<Boolean> waitForRecordUpdate(TableKey tableKey, KeyedTable table, Integer version) {
        TableKeyDefinition tableKeyDefinition = table.getTableKeyDefinition();
        List<String> fields = new ArrayList<>(tableKeyDefinition.getKeys());
        fields.add("version");
        Observable<OperatorEvent> observable = table.getOutput().observable(toArray(fields, String[]::new));
        return observable.filter(ev -> filterForVersionUpdate(ev,table.getPath(), tableKey,version, tableKeyDefinition)).take(1).timeout(5, TimeUnit.SECONDS, Observable.error(new RuntimeException("Unable to detect update to record " + tableKey + " version " + version + " after 5 seconds"))).map(res -> true);
    }

    private Boolean filterForVersionUpdate(OperatorEvent ev,String path, TableKey tableKey, Integer version, TableKeyDefinition tableKeyDefinition) {
        if(ev.getEventType().equals(EventType.ROW_ADD) || ev.getEventType().equals(EventType.ROW_UPDATE)){
            List<String> keys = tableKeyDefinition.getKeys();
            HashMap eventData = (HashMap) ev.getEventData();
            ev.getEventData();
            int count = keys.size();
            Object[] keyValues = new Object[count];
            for (int i = 0; i < count; i++) {
                keyValues[i] = eventData.get(keys.get(i));
            }
            TableKey key = new TableKey(keyValues);
            if(key.equals(tableKey)){
                Integer versionFromUpdate = (Integer) eventData.get("version");

                boolean result = version == null ? versionFromUpdate != null : versionFromUpdate > version;
                if(result){
                    logger.info(String.format("SUCCESS - Found update to table %s key %s waiting for update greater than version %s found %s",path,tableKey,version,versionFromUpdate));
                }else{
                    logger.info(String.format("INVALID - Found update to table %s key %s waiting for update greater than version %s found %s",path,tableKey,version,versionFromUpdate));
                }
                return result;
            }
        }
        return false;
    }
}
