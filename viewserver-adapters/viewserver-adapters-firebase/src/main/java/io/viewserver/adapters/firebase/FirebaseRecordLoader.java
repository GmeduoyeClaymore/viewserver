package io.viewserver.adapters.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.*;
import io.viewserver.datasource.*;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FirebaseRecordLoader implements IRecordLoader{
    private static final Logger logger = LoggerFactory.getLogger(FirebaseRecordLoader.class);
    private final FirebaseConnectionFactory connectionFactory;
    private String tableName;
    private SchemaConfig config;
    private OperatorCreationConfig creationConfig;
    boolean snapshotComplete;
    private CollectionReference collection;
    private ListenerRegistration listenerRegistration;

    public FirebaseRecordLoader(FirebaseConnectionFactory connectionFactory, String tableName, SchemaConfig config, OperatorCreationConfig creationConfig) {
        this.connectionFactory = connectionFactory;
        this.tableName = tableName;
        this.config = config;
        this.creationConfig = creationConfig;
    }

    @Override
    public OperatorCreationConfig getCreationConfig() {
        return creationConfig;
    }

    @Override
    public SchemaConfig getSchemaConfig() {
        return config;
    }

    public int loadRecords(IOperator query) {
        try{
            logger.info(String.format("Adding snapshot listener for Firebase table %s", tableName));
            EventListener<QuerySnapshot> listener = (snapshot, e) -> {
                if (e != null) {
                    logger.error(String.format("Listener failed for %s table", tableName), e);
                    return;
                }

                for (DocumentChange dc : snapshot.getDocumentChanges()) {
                    try {
                        if (snapshotComplete) {
                            logger.info(String.format("%s updates received from Firebase for table %s", snapshot.getDocumentChanges().size(), tableName));
                        }
                        //TODO - deal with removes
                        DocumentChangeRecord changeRecord = new DocumentChangeRecord(config, dc.getDocument());
                        RecordUtils.addRecordToTableOperator((KeyedTable) query,changeRecord);
                    } catch (Exception ex) {
                        logger.error(String.format("There was an error updating a record in the %s table", tableName), ex
                        );
                    }
                }
            };
            this.listenerRegistration = getCollection().addSnapshotListener(listener);
        }catch (Exception ex){
          throw new RuntimeException(ex);
        }
        return -1;
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
                        DocumentChangeRecord changeRecord = new DocumentChangeRecord(config, dc.getDocument());
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
            logger.error(String.format("Error adding snapshot listener for Firebase table %s \n{}", tableName), ex);
            throw new RuntimeException("Error getting records",ex);
        }
    }

    private void onFirebaseListenerError(FirestoreException e, Consumer<IRecord> consumer){
        logger.error(String.format("Listener failed for %s table re-adding listener", tableName), e);
        snapshotComplete = false;
        this.listenerRegistration.remove();
        addFirebaseListener(consumer);
    }

    protected CollectionReference getCollection(){
        if(this.collection == null){
            this.collection = getDb().collection(tableName);
        }
        return this.collection;
    }

    protected Firestore getDb(){
        return connectionFactory.getConnection();
    }


    @Override
    public void close(){
        if(this.listenerRegistration != null) {
            this.listenerRegistration.remove();
        }
    }
}


