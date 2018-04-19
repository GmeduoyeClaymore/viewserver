package io.viewserver.adapters.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.*;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IWritableDataAdapter;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.IRowFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private ListenerRegistration fireBaseListener;

    public FirebaseDataAdapter(String firebaseKeyPath, String tableName) {
        this.firebaseKeyPath = firebaseKeyPath;
        this.tableName = tableName;
    }

    @Override
    public int getRecords(String query, Consumer<IRecord> consumer) {
       return addFirebaseListener(consumer);
    }

    private int addFirebaseListener(Consumer<IRecord> consumer){
        CompletableFuture<Integer> ss = new CompletableFuture<>();

        try {
            logger.info(String.format("Adding snapshot listener for Firebase table %s", tableName));
            fireBaseListener = getCollection().addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    onFirebaseListenerError(e, consumer);
                    return;
                }

                String msg = snapshotComplete ? String.format("%s updates received from Firebase for table %s", snapshot.getDocumentChanges().size(), tableName) :
                        String.format("Snapshot with %s record from Firebase for table %s", snapshot.getDocumentChanges().size(), tableName);
                logger.info(msg);

                for (DocumentChange dc : snapshot.getDocumentChanges()) {
                    try {
                        //TODO - deal with removes
                        DocumentChangeRecord changeRecord = new DocumentChangeRecord(schema, dc.getDocument());
                        consumer.accept(changeRecord);
                    } catch (Exception ex) {
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
        fireBaseListener.remove();
        addFirebaseListener(consumer);
    }

    @Override
    public void insertRecord(ITableRow tableRow) {
    }

    @Override
    public void updateRecord(ITableRow tableRow, IRowFlags rowFlags) {
    }

    @Override
    public void deleteRecord(ITableRow tableRow) {
    }

    @Override
    public void clearData() {
        FirebaseUtils.deleteCollection(getCollection());
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    @JsonIgnore
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @Override
    @JsonIgnore
    public void setTableKeyDefinition(TableKeyDefinition tableKeyDefinition) {
        this.tableKeyDefinition = tableKeyDefinition;
    }

    @JsonIgnore
    protected CollectionReference getCollection(){
        return getDb().collection(tableName);
    }

    @JsonIgnore
    protected Firestore getDb(){
        if(connectionFactory == null){
            connectionFactory = new FirebaseConnectionFactory(firebaseKeyPath);
        }
        return connectionFactory.getConnection();
    }

    @Override
    @JsonIgnore
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


