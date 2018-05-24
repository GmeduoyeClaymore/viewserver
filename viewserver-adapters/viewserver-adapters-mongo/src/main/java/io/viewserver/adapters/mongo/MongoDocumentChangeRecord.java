package io.viewserver.adapters.mongo;

import io.viewserver.adapters.common.Record;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.datasource.SchemaConfig;
import org.bson.Document;

import java.sql.Timestamp;

/**
 * Created by Paul on 15/03/2018.
 */

public class MongoDocumentChangeRecord extends Record {
    private final SchemaConfig schema;

    public MongoDocumentChangeRecord(SchemaConfig schema, Document doc) {
        this.schema = schema;
        initialise(doc);
    }

    private void initialise(Document doc) {
        values.clear();

        for (Column column : schema.getColumns()) {
            String columnName = column.getName();
            if (!doc.containsKey(columnName)) {
                continue;
            }
            values.put(columnName, getMongoDocumentValue(column.getType(), columnName, doc));
        }
    }

    public Object getMongoDocumentValue(ContentType dataType, String colName, Document doc) {
        switch (dataType) {
            case Bool: {
                return doc.getBoolean(colName);
            }
            case Short: {
                return doc.getLong(colName).shortValue();
            }
            case Int: {
                return doc.getInteger(colName);
            }
            case Long: {
                return doc.getLong(colName);
            }
            case Float: {
                return doc.getDouble(colName).floatValue();
            }
            case Double: {
                return doc.getDouble(colName);
            }
            case String: {
                return doc.getString(colName);
            }
            case Json: {
                return doc.get(colName);
            }
            case DateTime: {
                return doc.getDate(colName) != null ? new java.sql.Date(doc.getDate(colName).getTime()) : null;
            }
            case Date: {
                return doc.getDate(colName) != null ? new Timestamp(doc.getDate(colName).getTime()) : null;
            }
            default:
                throw new RuntimeException(String.format("Could not process column of type %s", dataType));
        }
    }
}
