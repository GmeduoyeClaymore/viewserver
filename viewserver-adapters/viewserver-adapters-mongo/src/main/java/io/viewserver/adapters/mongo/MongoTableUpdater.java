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
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public void addOrUpdateRow(String tableName, SchemaConfig schemaConfig, IRecord record){
        try {
            log.info("Writing to table \"" + tableName + "\"");
            TableKeyDefinition definition = schemaConfig.getTableKeyDefinition();
            TableKey tableKey = RecordUtils.getTableKey(record,definition);
            String documentId = tableKey.toString("_");
            Map<String, Object> docData = getDocumentData(record, schemaConfig);
            BasicDBObject query = new BasicDBObject("_id",documentId);
            getCollection(tableName).replaceOne(query, new Document(docData),new UpdateOptions().upsert(true));
            log.info("Finished Writing to table \"" + tableName + "\"");
        }catch (Exception ex){
            log.error("Writing to \"" + tableName + "\" failed exception is",ex);
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
