package io.viewserver.adapters.firebase;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import io.viewserver.adapters.common.Record;
import io.viewserver.datasource.ColumnType;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnMetadata;

import java.sql.Timestamp;

/**
 * Created by Paul on 15/03/2018.
 */

public class DocumentChangeRecord extends Record {
    private final Schema schema;

    public DocumentChangeRecord(Schema schema, QueryDocumentSnapshot doc) {
        this.schema = schema;
        initialise(doc);
    }

    private void initialise(QueryDocumentSnapshot doc) {
        values.clear();

        for (ColumnHolder columnHolder : schema.getColumnHolders()) {
            try {
                io.viewserver.datasource.ColumnType dataType = null;
                ColumnMetadata metadata = columnHolder.getMetadata();
                if (metadata != null) {
                    if (metadata.isFlagged(ColumnFlags.DATASOURCE_CALCULATION)) {
                        continue;
                    }
                    dataType = metadata.getDataType();
                }
                if (dataType == null) {
                    dataType = mapToDataType(columnHolder.getType());
                }

                values.put(columnHolder.getName(), getFirebaseDocumentValue(dataType, columnHolder.getName(), doc));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
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

    protected io.viewserver.datasource.ColumnType mapToDataType(io.viewserver.schema.column.ColumnType columnType) {
        switch (columnType) {
            case Bool:
                return io.viewserver.datasource.ColumnType.Bool;
            case NullableBool:
                return io.viewserver.datasource.ColumnType.NullableBool;
            case Byte:
                return io.viewserver.datasource.ColumnType.Byte;
            case Short:
                return io.viewserver.datasource.ColumnType.Short;
            case Int:
                return io.viewserver.datasource.ColumnType.Int;
            case Long:
                return io.viewserver.datasource.ColumnType.Long;
            case Float:
                return io.viewserver.datasource.ColumnType.Float;
            case Double:
                return io.viewserver.datasource.ColumnType.Double;
            case String:
                return io.viewserver.datasource.ColumnType.String;
            default:
                throw new IllegalArgumentException(String.format("Unknown column type '%s'", columnType));
        }
    }
}
