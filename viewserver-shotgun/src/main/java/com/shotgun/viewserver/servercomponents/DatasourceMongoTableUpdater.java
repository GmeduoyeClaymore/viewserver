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
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.viewserver.core.Utils.toArray;



public class DatasourceMongoTableUpdater extends MongoTableUpdater {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceMongoTableUpdater.class);
    private ConcurrentHashMap<TableUpdateKey,Observable<Boolean>> inFlightUpdates = new ConcurrentHashMap<>();
    private ICatalog catalog;

    public DatasourceMongoTableUpdater(MongoConnectionFactory connectionFactory, ICatalog catalog) {
        super(connectionFactory);
        this.catalog = catalog;
    }

    @Override
    public synchronized Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record, Integer version){

        KeyedTable table = (KeyedTable) catalog.getOperatorByPath(tableName);
        TableKeyDefinition definition = schemaConfig.getTableKeyDefinition();
        TableKey tableKey = RecordUtils.getTableKey(record, definition);
        TableUpdateKey key = new TableUpdateKey(tableName, tableKey);
        DataSourceTableName dsTableName = new DataSourceTableName(tableName);

        if(!anyFieldsHaveChanged(record,tableKey,table)){
            return Observable.just(true);
        }

        Observable<Boolean> inFlightUpdate = this.inFlightUpdates.get(key);
        if(inFlightUpdate != null){
            logger.info(String.format("Found in flight update for table %s record %s",table,tableKey));
            Observable<Boolean> booleanObservable = inFlightUpdate.flatMap(res -> {
                if (res) {
                    logger.info(String.format("In flight update for table %s record %s completed successfully", table, tableKey));
                    return getBooleanObservable(tableName, schemaConfig, record, table, tableKey, key, dsTableName, version);
                } else {
                    String format = String.format("In flight update for table %s record %s failed barfing", table, tableKey);
                    logger.info(format);
                    throw new RuntimeException(format);
                }
            });
            this.inFlightUpdates.put(key,booleanObservable);
            return booleanObservable;
        }


        return getBooleanObservable(tableName, schemaConfig, record, table, tableKey, key, dsTableName, version);
    }

    private boolean anyFieldsHaveChanged(IRecord record, TableKey key, KeyedTable table) {
        for(String field : record.getColumnNames()){
            Object recordValue = record.getValue(field);
            Object columnValue = ColumnHolderUtils.getColumnValue(table, field,key);
            if(recordValue == null && columnValue == null){
                continue;
            }
            if(recordValue == null || columnValue == null){
                return true;
            }
            if(!recordValue.equals(ConvertUtils.convert(columnValue,recordValue.getClass()))){
                return true;
            }
        }
        return false;
    }

    private Observable<Boolean> getBooleanObservable(String tableName, SchemaConfig schemaConfig, IRecord record, KeyedTable table, TableKey tableKey, TableUpdateKey key, DataSourceTableName dsTableName, Integer version) {
        Integer versionBeforeUpdate = getVersionBeforeUpdate(table, version, tableKey);

        if(logger.isDebugEnabled()) {
            logger.debug("Updatating {} with record {}", table, record.asString());
        }
        Observable<Boolean> booleanObservable = super.addOrUpdateRow(dsTableName.getDataSourceName(), schemaConfig, record, versionBeforeUpdate).flatMap(res -> {
            if (!res) {
                throw new RuntimeException("Update to record " + tableKey + " has not been acknowleged");
            }
            return waitForRecordUpdate(tableName,tableKey, table, versionBeforeUpdate);
        });
        inFlightUpdates.put(key,booleanObservable);
        return booleanObservable;
    }

    private Integer getVersionBeforeUpdate(KeyedTable table, Integer version, TableKey tableKey) {
        if(version != null && version == -1){
            version = (Integer) ColumnHolderUtils.getColumnValue(table,"version", tableKey);
            if(version == 0){
                version = null;
            }
            logger.info("Found record {} had the magic version number -1 so got the current verison from table which is \"{}\"",tableKey,version);
        }
        return new Integer(-1).equals(version) ? null : version;
    }


    private Observable<Boolean> waitForRecordUpdate(String tableName,TableKey tableKey, KeyedTable table, Integer version) {
        TableKeyDefinition tableKeyDefinition = table.getTableKeyDefinition();
        List<String> fields = new ArrayList<>(tableKeyDefinition.getKeys());
        fields.add("version");
        Observable<OperatorEvent> observable = table.getOutput().observable(toArray(fields, String[]::new));
         return observable.filter(ev -> filterForVersionUpdate(ev,table.getPath(), tableKey,version, tableKeyDefinition)).take(1).timeout(5, TimeUnit.SECONDS, isStopped ? Observable.empty() : Observable.error(new RuntimeException(getMessage(tableKey, version)))).map(
                res -> {
                    inFlightUpdates.remove(new TableUpdateKey(tableName,tableKey));
                    return true;
                });
    }

    private String getMessage(TableKey tableKey, Integer version) {
        String s = "Unable to detect update to record " + tableKey + " version " + version + " after 5 seconds";
        logger.error(s);
        return s;
    }

    private Boolean filterForVersionUpdate(OperatorEvent ev,String path, TableKey tableKey, Integer version, TableKeyDefinition tableKeyDefinition) {
        if(ev.getEventType().equals(EventType.ROW_ADD) || ev.getEventType().equals(EventType.ROW_UPDATE)){
            List<String> keys = tableKeyDefinition.getKeys();
            HashMap eventData = (HashMap) ev.getEventData();
            int count = keys.size();
            Object[] keyValues = new Object[count];
            for (int i = 0; i < count; i++) {
                keyValues[i] = eventData.get(keys.get(i));
            }
            TableKey key = new TableKey(keyValues);
            if(key.equals(tableKey)){
                Integer versionFromUpdate = (Integer) eventData.get("version");

                boolean result = new Integer(Integer.MAX_VALUE).equals(version)  || version == null ? versionFromUpdate != null : versionFromUpdate > version;
                if(result){
                    logger.info(String.format("SUCCESS - Found update to table %s key %s waiting for update greater than version %s found %s",path,tableKey,version,versionFromUpdate));
                    logger.info("Record is :" + ev.getEventData());
                }else{
                    logger.info(String.format("INVALID - Found update to table %s key %s waiting for update greater than version %s found %s",path,tableKey,version,versionFromUpdate));
                }
                return result;
            }
        }
        return false;
    }


    @Override
    public void stop() {
        try {
            if (inFlightUpdates.size() > 0) {
                Observable.zip(inFlightUpdates.values(), objects -> null).take(1).timeout(10, TimeUnit.SECONDS).toBlocking().first();
            }
        }catch (Exception ex){
            logger.error(ex.getMessage());
        }
        super.stop();
    }

    class TableUpdateKey{
        private String tableName;
        private TableKey tableKey;

        public TableUpdateKey(String tableName, TableKey tableKey) {
            this.tableName = tableName;
            this.tableKey = tableKey;
        }

        public String getTableName() {
            return tableName;
        }

        public TableKey getTableKey() {
            return tableKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TableUpdateKey that = (TableUpdateKey) o;
            return Objects.equals(tableName, that.tableName) &&
                    Objects.equals(tableKey, that.tableKey);
        }

        @Override
        public int hashCode() {

            return Objects.hash(tableName, tableKey);
        }
    }
}
