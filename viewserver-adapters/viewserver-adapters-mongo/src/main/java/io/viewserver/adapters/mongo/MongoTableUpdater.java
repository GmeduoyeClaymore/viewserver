package io.viewserver.adapters.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.controller.ControllerUtils;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MongoTableUpdater implements IDatabaseUpdater {
    private MongoConnectionFactory connectionFactory;
    private static final Logger log = LoggerFactory.getLogger(MongoTableUpdater.class);

    public MongoTableUpdater(MongoConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        return Observable.create(
                booleanEmitter -> {
                    try {
                        TableKeyDefinition definition = schemaConfig.getTableKeyDefinition();
                        TableKey tableKey = RecordUtils.getTableKey(record, definition);
                        String documentId = tableKey.toString("_");
                        Map<String, Object> docData = getDocumentData(record, schemaConfig);
                        Integer versionBeforeUpdate = record.getInt("version");
                        docData.put("version", incrementVersion(record));
                        log.info("Writing to table \"" + tableName + "\" record id " + documentId + " initial version is " + record.getValue("version") + " new version is " + docData.get("version"));
                        Document tDocument = new Document(docData);
                        boolean result = getUpdateResult(versionBeforeUpdate, tableName, documentId, tDocument);
                        log.info("Finished Writing to table \"" + tableName + "\" record id " + documentId + " initial version is " + record.getValue("version") + " new version is " + docData.get("version"));
                        booleanEmitter.onNext(result);
                        booleanEmitter.onCompleted();
                    }catch (Exception ex){
                        log.error("Writing to \"" + tableName + "\" failed exception is",ex);
                        booleanEmitter.onError(ex);
                    }
                }, Emitter.BackpressureMode.BUFFER
        );
    }

    public Integer incrementVersion(IRecord record) {
        Integer version = (Integer) record.getValue("version");
        if(version == null){
            return 0;
        }
        return version + 1;
    }

    public boolean getUpdateResult(Integer initialVersion, String tableName, String  documentId, Document tDocument) {
        Bson updateQuery = new Document("$set", tDocument);
        Integer version = tDocument.getInteger("version");
        if(version == 0){
            tDocument.put("_id",documentId);
            getCollection(tableName).insertOne(tDocument);
            return true;
        }else{
            BasicDBObject query = new BasicDBObject("_id", documentId);
            query.put("version", new BasicDBObject("$eq", initialVersion));
            return getCollection(tableName).updateOne(query, updateQuery,new UpdateOptions()).wasAcknowledged();


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

    protected MongoCollection<Document> getCollection(String tableName){
        return getDb().getCollection(tableName);
    }

    protected MongoDatabase getDb(){
        return connectionFactory.getConnection();
    }

}
