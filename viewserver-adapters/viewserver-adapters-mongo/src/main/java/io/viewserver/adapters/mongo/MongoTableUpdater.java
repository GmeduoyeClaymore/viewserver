package io.viewserver.adapters.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import io.viewserver.adapters.common.IDatabaseUpdater;
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
    public Observable<Boolean> addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record, Integer versionToBeUpdated){
        return Observable.create(
                booleanEmitter -> {
                    try {
                        TableKeyDefinition definition = schemaConfig.getTableKeyDefinition();
                        TableKey tableKey = RecordUtils.getTableKey(record, definition);
                        String documentId = tableKey.toString("_");
                        Map<String, Object> docData = getDocumentData(record, schemaConfig);
                        docData.put("version", incrementVersion(versionToBeUpdated));
                        log.info("Writing to table \"" + tableName + "\" record id " + documentId + " initial version is " + versionToBeUpdated + " new version is " + docData.get("version"));
                        log.info("Writing record to table " + docData);
                        Document tDocument = new Document(docData);
                        boolean result = getUpdateResult(versionToBeUpdated, tableName, documentId, tDocument);
                        log.info("Record result is " + result);
                        log.info("Finished Writing to table \"" + tableName + "\" record id " + documentId + " initial version is " + versionToBeUpdated + " new version is " + docData.get("version"));
                        booleanEmitter.onNext(result);
                        booleanEmitter.onCompleted();
                    }catch (Exception ex){
                        log.error("Writing to \"" + tableName + "\" failed exception is",ex);
                        booleanEmitter.onError(ex);
                    }
                }, Emitter.BackpressureMode.BUFFER
        );
    }

    private Integer getVersionBeforeUpdate(IRecord record) {
        return record.getInt("version");
    }

    public Integer incrementVersion(Integer version) {
        if(version == null){
            return 1;
        }
        if(version == Integer.MAX_VALUE){
            return version;
        }
        return version + 1;
    }

    public boolean getUpdateResult(Integer versionToBeUpdated, String tableName, String  documentId, Document tDocument) {
        Bson updateQuery = new Document("$set", tDocument);
        Integer version = tDocument.getInteger("version");
        if(versionToBeUpdated == null){
            log.info(documentId + " Record version is 0 so treating as an insert");
            tDocument.put("_id",documentId);
            getCollection(tableName).insertOne(tDocument);
            return true;
        }
        else if(version == Integer.MAX_VALUE){
            log.info(documentId + " Record version is int.MaxValue so treating as an unconditional upsert");
            BasicDBObject query = new BasicDBObject("_id",documentId);
            getCollection(tableName).replaceOne(query, tDocument,new UpdateOptions().upsert(true));
            return true;
        }
        else{
            log.info(documentId + " Record version is "+version+" so treating as an update to a specific version");
            BasicDBObject query = new BasicDBObject("_id", documentId);
            query.put("version", new BasicDBObject("$eq", versionToBeUpdated));
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
