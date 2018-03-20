package io.viewserver.adapters.firebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IWritableDataAdapter;
import io.viewserver.operators.table.ITableRow;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.IRowFlags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FirebaseDataAdapter implements IWritableDataAdapter {
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
            getCollection().addSnapshotListener((snapshot, e) -> {
                for (DocumentChange dc : snapshot.getDocumentChanges()) {
                    //TODO - deal with adds, updates and removes
                    consumer.accept(new DocumentChangeRecord(schema, dc.getDocument()));
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
        String documentId = getDocumentId(tableRow);
        Map<String, Object> docData = getDocumentData(tableRow);
        ApiFuture<WriteResult> future = getCollection().document(documentId).set(docData);

        //TODO - add result listeners here
    }

    @Override
    public void updateRecord(ITableRow tableRow, IRowFlags rowFlags) {
        String documentId = getDocumentId(tableRow);
        Map<String, Object> docData = getDocumentData(tableRow);
        ApiFuture<WriteResult> future = getCollection().document(documentId).set(docData, SetOptions.merge());
        //TODO - add result listeners here
    }

    @Override
    public void deleteRecord(ITableRow tableRow) {
        String documentId = getDocumentId(tableRow);
        ApiFuture<WriteResult> future = getCollection().document(documentId).delete();
        //TODO - add result listeners here
    }

    @Override
    public void clearData() {
        CollectionReference collection = getCollection();
        FirebaseUtils.deleteCollection(collection, 1000);
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
        if(connectionFactory == null){
            connectionFactory = new FirebaseConnectionFactory(firebaseKeyPath);
        }
        return connectionFactory.getConnection().collection(tableName);
    }

    @JsonIgnore
    private String getDocumentId(ITableRow tableRow){
        List<String> values = new ArrayList<>();

        for(String key: tableKeyDefinition.getKeys()){
            values.add(getValue(key, tableRow).toString());
        }

        return String.join("_", values);
    }

    @JsonIgnore
    private Map<String, Object> getDocumentData(ITableRow tableRow){
        Map<String, Object> docData = new HashMap<>();
        List<ColumnHolder> columnHolders = schema.getColumnHolders();

        for(ColumnHolder columnHolder: columnHolders){
            docData.put(columnHolder.getName(), getValue(columnHolder.getName(), tableRow));
        }

        return docData;
    }

    @JsonIgnore
    private Object getValue(String column, ITableRow tableRow) {
        ColumnHolder columnHolder = schema.getColumnHolder(column);

        switch (columnHolder.getType()) {
            case Bool: {
                return tableRow.getBool(columnHolder.getName());
            }
            case Byte: {
                return tableRow.getByte(columnHolder.getName());
            }
            case Short: {
                return tableRow.getShort(columnHolder.getName());
            }
            case Int: {
                return tableRow.getInt(columnHolder.getName());
            }
            case Long: {
                return tableRow.getLong(columnHolder.getName());
            }
            case Float: {
                return tableRow.getFloat(columnHolder.getName());
            }
            case Double: {
                return  tableRow.getDouble(columnHolder.getName());
            }
            case String: {
                return tableRow.getString(columnHolder.getName());
            }
            default:
                throw new RuntimeException(String.format("Could not process column of type %s", columnHolder.getType()));
        }
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


