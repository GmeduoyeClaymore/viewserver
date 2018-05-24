package io.viewserver.adapters.mongo;

import io.viewserver.datasource.ContentType;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnMetadata;

public class MongoUtils {

    public static ContentType getDataType(ColumnHolder columnHolder){
        ContentType dataType = null;
        ColumnMetadata metadata = columnHolder.getMetadata();
        if (metadata != null) {
            if (metadata.isFlagged(ColumnFlags.DATASOURCE_CALCULATION)) {
                return null;
            }
            dataType = metadata.getDataType();
        }
        if (dataType == null) {
            dataType = MongoUtils.mapToDataType(columnHolder.getType());
        }

        return dataType;
    }

    public static ContentType mapToDataType(io.viewserver.schema.column.ColumnType columnType) {
        switch (columnType) {
            case Bool:
                return ContentType.Bool;
            case NullableBool:
                return ContentType.NullableBool;
            case Byte:
                return ContentType.Byte;
            case Short:
                return ContentType.Short;
            case Int:
                return ContentType.Int;
            case Long:
                return ContentType.Long;
            case Float:
                return ContentType.Float;
            case Double:
                return ContentType.Double;
            case String:
                return ContentType.String;
            default:
                throw new IllegalArgumentException(String.format("Unknown column type '%s'", columnType));
        }
    }
}
