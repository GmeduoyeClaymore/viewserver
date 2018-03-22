package com.shotgun.viewserver;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.ColumnType;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.reactor.IReactor;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDatabaseUpdater implements IDatabaseUpdater {
    private final IReactor reactor;
    private FirebaseConnectionFactory connectionFactory;
    private String firebaseKeyPath;
    private static final Logger log = LoggerFactory.getLogger(FirebaseDatabaseUpdater.class);

    public FirebaseDatabaseUpdater(IExecutionContext executionContext, String firebaseKeyPath) {
        this.firebaseKeyPath = firebaseKeyPath;
        this.reactor = executionContext.getReactor();
    }

    @Override
    public void addOrUpdateRow(String tableName, String dataSourceName, IRecord record){
        KeyedTable table = ControllerUtils.getKeyedTable(tableName);
        addOrUpdateRow(table, dataSourceName, record);
    }

    @Override
    public void addOrUpdateRow(KeyedTable table, String dataSourceName, IRecord record) {
        if(!Thread.currentThread().getName().startsWith("reactor-")){
            throw new RuntimeException("This code is being called from a non reactor thread this is wrong");
        }

        try {
            TableKey tableKey = table.getTableKey(record);
            int rowId = table.getRow(tableKey);
            String documentId = getDocumentId(record, table);
            Map<String, Object> docData = getDocumentData(record, table.getOutput().getSchema());

            DocumentReference document = getCollection(dataSourceName).document(documentId);
            ApiFuture<WriteResult> future;
            if (rowId != -1) {
                future = document.set(docData, SetOptions.merge());
            } else {
                future = document.set(docData);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

        //TODO - add result listener
    }

    @Override
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

    private String getDocumentId(IRecord record, KeyedTable table){
        List<String> values = new ArrayList<>();

        for(String key: table.getTableKeyDefinition().getKeys()){
            try {
                values.add(getRecordValue(key, record, table.getOutput().getSchema()).toString());
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }

        return String.join("_", values);
    }

    private Map<String, Object> getDocumentData(IRecord record, Schema schema){
        Map<String, Object> docData = new HashMap<>();
        List<ColumnHolder> columnHolders = schema.getColumnHolders();

        for(ColumnHolder columnHolder: columnHolders){
            String colName = columnHolder.getName();
            if(!record.hasValue(colName)){
                continue;
            }

            docData.put(colName, getRecordValue(colName, record, schema));
        }

        return docData;
    }

    private Object getRecordValue(String column, IRecord record, Schema schema) {
        ColumnHolder columnHolder = schema.getColumnHolder(column);
        ColumnType dataType = FirebaseUtils.getDataType(columnHolder);
        String columnName = columnHolder.getName();

        try {
            switch (dataType) {
                case Bool: {
                    return record.getBool(columnName);
                }
                case Byte: {
                    return record.getString(columnName);
                }
                case Short: {
                    return record.getShort(columnName);
                }
                case Int: {
                    return record.getInt(columnName);
                }
                case Long: {
                    return record.getLong(columnName);
                }
                case Float: {
                    return record.getFloat(columnName);
                }
                case Double: {
                    return record.getDouble(columnName);
                }
                case String: {
                    return record.getString(columnName);
                }
                case DateTime: {
                    return record.getDateTime(columnName) != null ? record.getDateTime(columnName) : null;
                }
                case Date: {
                    return record.getDateTime(columnName) != null ? record.getDateTime(columnName) : null;
                }
                default:
                    throw new RuntimeException(String.format("Could not process column of type %s", columnHolder.getType()));
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    protected CollectionReference getCollection(String tableName){
        return getDb().collection(tableName);
    }

    protected Firestore getDb(){
        if(connectionFactory == null){
            connectionFactory = new FirebaseConnectionFactory(firebaseKeyPath);
        }
        return connectionFactory.getConnection();
    }

  /*  @Override
    public void insertRecord(ITableRow tableRow) {
        String documentId = getDocumentId(tableRow);
        Map<String, Object> docData = getDocumentData(tableRow);
        ApiFuture<WriteResult> future = getCollection().document(documentId).set(docData);

        //TODO - add result listeners here
    }

    @Override
    public void updateRecord(ITableRow tableRow, IRowFlags rowFlags) {
        String documentId = getDocumentId(tableRow);
        Map<String, Object> docData = getDocumentData(tableRow);
        ApiFuture<WriteResult> future = getCollection().document(documentId).set(docData, SetOptions.merge());
        //TODO - add result listeners here
    }

    @Override
    public void deleteRecord(ITableRow tableRow) {
        String documentId = getDocumentId(tableRow);
        ApiFuture<WriteResult> future = getCollection().document(documentId).delete();
        //TODO - add result listeners here
    }*/
}
