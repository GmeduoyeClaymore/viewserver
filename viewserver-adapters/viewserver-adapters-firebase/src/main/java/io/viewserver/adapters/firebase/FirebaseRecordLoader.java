package io.viewserver.adapters.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import io.viewserver.datasource.*;
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

    public Observable<IRecord> getRecords(String query) {
        return  rx.Observable.create(subscriber -> {
            try{
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
                            DocumentChangeRecord changeRecord = new DocumentChangeRecord(config, dc.getDocument());
                            subscriber.onNext(changeRecord);
                        }catch (Exception ex){
                            logger.error(String.format("There was an error updating a record in the %s table", tableName));
                        }
                    }
                });
            }catch (Exception ex){
                subscriber.onError(ex);
            }}, Emitter.BackpressureMode.BUFFER);

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
            logger.error(String.format("Error adding snapshot listener for Firebase table %s", tableName));
            throw new RuntimeException("Error getting records");
        }
    }

    private void onFirebaseListenerError(FirestoreException e, Consumer<IRecord> consumer){
        logger.error(String.format("Listener failed for %s table re-adding listener", tableName), e);
        snapshotComplete = false;
        addFirebaseListener(consumer);
    }

    protected CollectionReference getCollection(){
        return getDb().collection(tableName);
    }

    protected Firestore getDb(){
        return connectionFactory.getConnection();
    }

}


