package io.viewserver.adapters.firebase;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import io.viewserver.adapters.common.Record;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ColumnType;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnMetadata;

import java.sql.Timestamp;

/**
 * Created by Paul on 15/03/2018.
 */

public class DocumentChangeRecord extends Record {
    private final SchemaConfig schema;

    public DocumentChangeRecord(SchemaConfig schema, QueryDocumentSnapshot doc) {
        this.schema = schema;
        initialise(doc);
    }

    private void initialise(QueryDocumentSnapshot doc) {
        values.clear();

        for (Column column : schema.getColumns()) {
            String columnName = column.getName();
            if (!doc.contains(columnName)) {
                continue;
            }
            values.put(columnName, getFirebaseDocumentValue(column.getType(), columnName, doc));
        }
    }

    public Object getFirebaseDocumentValue(ColumnType dataType, String colName, QueryDocumentSnapshot doc) {
        switch (dataType) {
            case Bool: {
                return doc.getBoolean(colName);
            }
            case Short: {
                return doc.getLong(colName).shortValue();
            }
            case Int: {
                return doc.getLong(colName).intValue();
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
