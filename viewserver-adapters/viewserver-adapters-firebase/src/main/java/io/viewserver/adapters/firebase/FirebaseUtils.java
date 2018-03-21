package io.viewserver.adapters.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import io.viewserver.datasource.ColumnType;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnMetadata;

import java.util.List;

public class FirebaseUtils{
    public static void deleteCollection(CollectionReference collection) {
        try {
            WriteBatch batch = collection.getFirestore().batch();
            ApiFuture<QuerySnapshot> future = collection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                batch.delete(document.getReference());
            }
            batch.commit().get();
        } catch (Exception e) {
            System.err.println("Error deleting collection : " + e.getMessage());
        }
    }

    public static ColumnType getDataType(ColumnHolder columnHolder){
        io.viewserver.datasource.ColumnType dataType = null;
        ColumnMetadata metadata = columnHolder.getMetadata();
        if (metadata != null) {
            if (metadata.isFlagged(ColumnFlags.DATASOURCE_CALCULATION)) {
                return null;
            }
            dataType = metadata.getDataType();
        }
        if (dataType == null) {
            dataType = FirebaseUtils.mapToDataType(columnHolder.getType());
        }

        return dataType;
    }

    public static io.viewserver.datasource.ColumnType mapToDataType(io.viewserver.schema.column.ColumnType columnType) {
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
