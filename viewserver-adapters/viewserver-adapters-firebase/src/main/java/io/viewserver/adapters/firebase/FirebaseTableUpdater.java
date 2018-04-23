package com.shotgun.viewserver.servercomponents;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
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


public class FirebaseTableUpdater implements IDatabaseUpdater {
    private FirebaseConnectionFactory connectionFactory;
    private static final Logger log = LoggerFactory.getLogger(FirebaseTableUpdater.class);

    public FirebaseTableUpdater(FirebaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        try {
            DataSourceTableName dataSourceTableName = new DataSourceTableName(tableName);
            TableKeyDefinition definition = new TableKeyDefinition(schemaConfig.getKeyColumns().toArray(new String[schemaConfig.getKeyColumns().size()]));
            TableKey tableKey = RecordUtils.getTableKey(record,definition);
            String documentId = tableKey.toString("_");
            Map<String, Object> docData = getDocumentData(record, schemaConfig);
            DocumentReference document = getCollection(dataSourceTableName.getDataSourceName()).document(documentId);
            document.set(docData, SetOptions.merge());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Observable<Boolean> scheduleAddOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        addOrUpdateRow(tableName,schemaConfig,record);
        return Observable.just(true);
    }


    private Map<String, Object> getDocumentData(IRecord record, SchemaConfig config){
        Map<String, Object> docData = new HashMap<>();
        List<Column> columns = config.getColumns();

        for(Column columnHolder: columns){
            String colName = columnHolder.getName();
            if(!record.hasValue(colName)){
                continue;
            }

            docData.put(colName, getRecordValue(columnHolder, record));
        }

        return docData;
    }

    private Object getRecordValue(Column column, IRecord record) {
        ContentType dataType = column.getType();
        String columnName = column.getName();
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
                    throw new RuntimeException(String.format("Could not process column of type %s", column.getType()));
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    protected CollectionReference getCollection(String tableName){
        return getDb().collection(tableName);
    }

    protected Firestore getDb(){
        return connectionFactory.getConnection();
    }

}
