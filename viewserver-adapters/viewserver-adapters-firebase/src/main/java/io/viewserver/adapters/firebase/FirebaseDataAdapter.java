package io.viewserver.adapters.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
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

    public FirebaseDataAdapter(String firebaseKeyPath, String tableName) {
        this.firebaseKeyPath = firebaseKeyPath;
        this.tableName = tableName;
    }

    @Override
    public int getRecords(String query, Consumer<IRecord> consumer) {
        CompletableFuture<Integer> ss = new CompletableFuture<>();

        try {
            logger.info(String.format("Adding snapshot listener for Firebase table %s", tableName));
            getCollection().addSnapshotListener((snapshot, e) -> {
                if(snapshotComplete) {
                    logger.info(String.format("%s updates received from Firebase for table %s", snapshot.getDocumentChanges().size(), tableName));
                }

                for (DocumentChange dc : snapshot.getDocumentChanges()) {
                    //TODO - deal with removes
                    DocumentChangeRecord changeRecord = new DocumentChangeRecord(schema, dc.getDocument());
                    consumer.accept(changeRecord);
                }

                if (!snapshotComplete) {
                    ss.complete(snapshot.size());
                    snapshotComplete = true;
                }
            });
            return ss.get();
        }catch(Exception ex){
            throw new RuntimeException("Error getting records");
        }
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


