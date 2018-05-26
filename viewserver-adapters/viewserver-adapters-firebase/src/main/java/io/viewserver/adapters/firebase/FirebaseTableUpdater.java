package io.viewserver.adapters.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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
    public Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        try {
            log.info("Writing to table \"" + tableName + "\"");
            TableKeyDefinition definition = schemaConfig.getTableKeyDefinition();
            TableKey tableKey = RecordUtils.getTableKey(record,definition);
            String documentId = tableKey.toString("_");
            Map<String, Object> docData = getDocumentData(record, schemaConfig);
            DocumentReference document = getCollection(tableName).document(documentId);
            document.set(docData, SetOptions.merge());
            log.info("Finished Writing to table \"" + tableName + "\"");
            return Observable.just(true);
        }catch (Exception ex){
            log.error("Writing to \"" + tableName + "\" failed exception is",ex);
            throw new RuntimeException(ex);
        }
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
                case Json: {
                    return record.getValue(columnName);
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
