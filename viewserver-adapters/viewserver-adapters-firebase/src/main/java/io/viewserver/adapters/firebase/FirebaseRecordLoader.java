package io.viewserver.adapters.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseException;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IWritableDataAdapter;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FirebaseDataAdapter implements IWritableDataAdapter {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseDataAdapter.class);
    private String firebaseKeyPath;
    private String tableName;
    protected DataSource dataSource;
    protected TableKeyDefinition tableKeyDefinition;
    protected Schema schema;
    boolean snapshotComplete;
    private FirebaseConnectionFactory connectionFactory;

    public FirebaseDataAdapter(FirebaseConnectionFactory connectionFactory, String tableName) {
        this.connectionFactory = connectionFactory;
        this.tableName = tableName;
    }

    @Override
    public int getRecords(String query, Consumer<IRecord> consumer) {
        CompletableFuture<Integer> ss = new CompletableFuture<>();

        try {
            logger.info(String.format("Adding snapshot listener for Firebase table %s", tableName));
            getCollection().addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    logger.error(String.format("Listener failed for %s table", tableName), e);
                    return;
                }

                for (DocumentChange dc : snapshot.getDocumentChanges()) {
                    try {
                        if(snapshotComplete) {
                            logger.info(String.format("%s updates received from Firebase for table %s", snapshot.getDocumentChanges().size(), tableName));
                        }
                        //TODO - deal with removes
                        DocumentChangeRecord changeRecord = new DocumentChangeRecord(schema, dc.getDocument());
                        consumer.accept(changeRecord);
                    }catch (Exception ex){
                        logger.error(String.format("There was an error updating a record in the %s table", tableName));
                    }
                }

                if (!snapshotComplete) {
                    ss.complete(snapshot.size());
                    snapshotComplete = true;
                }
            });
            return ss.get();
        }catch(Exception ex){
            logger.error(String.format("Error adding snapshot listener for Firebase table %s", tableName));
            throw new RuntimeException("Error getting records");
        }
    }

    private int addFirebaseListener(Consumer<IRecord> consumer){
        CompletableFuture<Integer> ss = new CompletableFuture<>();

        try {
            logger.info(String.format("Adding snapshot listener for Firebase table %s", tableName));
            getCollection().addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    onFirebaseListenerError(e, consumer);
                    return;
                }

                for (DocumentChange dc : snapshot.getDocumentChanges()) {
                    try {
                        if(snapshotComplete) {
                            logger.info(String.format("%s updates received from Firebase for table %s", snapshot.getDocumentChanges().size(), tableName));
                        }
                        //TODO - deal with removes
                        DocumentChangeRecord changeRecord = new DocumentChangeRecord(schema, dc.getDocument());
                        consumer.accept(changeRecord);
                    }catch (Exception ex){
                        logger.error(String.format("There was an error updating a record in the %s table", tableName));
                    }
                }

                if (!snapshotComplete) {
                    ss.complete(snapshot.size());
                    snapshotComplete = true;
                }
            });
            return ss.get();
        }catch(Exception ex){
            logger.error(String.format("Error adding snapshot listener for Firebase table %s", tableName));
            throw new RuntimeException("Error getting records");
        }
    }

    private void onFirebaseListenerError(FirestoreException e, Consumer<IRecord> consumer){
        logger.error(String.format("Listener failed for %s table re-adding listener", tableName), e);
        snapshotComplete = false;
        addFirebaseListener(consumer);
    }


    public TableKeyDefinition getDerivedTableKeyDefinition() {
        return null;
    }

    @Override
    @JsonIgnore
    public Schema getDerivedSchema() {return null;}

    public String getFirebaseKeyPath() {
        return firebaseKeyPath;
    }

    public void setFirebaseKeyPath(String firebaseKeyPath) {
        this.firebaseKeyPath = firebaseKeyPath;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}


