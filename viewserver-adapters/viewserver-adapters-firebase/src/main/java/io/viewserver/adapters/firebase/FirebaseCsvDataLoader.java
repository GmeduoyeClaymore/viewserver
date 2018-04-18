package io.viewserver.adapters.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import io.viewserver.adapters.ILoader;
import io.viewserver.adapters.csv.CsvRecordWrapper;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.table.TableKeyDefinition;
import javolution.io.UTF8StreamReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseCsvDataLoader implements ILoader {
    private static final Logger log = LoggerFactory.getLogger(FirebaseCsvDataLoader.class);
    private CsvRecordWrapper recordWrapper;
    private String fileName;
    private String tableName;
    private TableKeyDefinition tableKey;
    private FirebaseConnectionFactory firebaseConnectionFactory;

    public FirebaseCsvDataLoader(String fileName, String tableName, SchemaConfig config,TableKeyDefinition tableKey, FirebaseConnectionFactory firebaseConnectionFactory) {
        this.fileName = fileName;
        this.tableName = tableName;
        this.tableKey = tableKey;
        this.firebaseConnectionFactory = firebaseConnectionFactory;
        this.recordWrapper = new CsvRecordWrapper(new DateTime(DateTimeZone.UTC),config);
    }


    public int load() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        WriteBatch batch = getDb().batch();
        int recordsLoaded = 0;
        try {
            UTF8StreamReader reader = new UTF8StreamReader();
            reader.setInput(stream);
            try {
                CSVParser parser = CSVFormat.EXCEL.withHeader().parse(reader);

                for (final CSVRecord record : parser) {
                    insertCsvRecord(record, batch);
                    recordsLoaded++;
                }
            } finally {
                batch.commit().get();
                reader.close();
            }
            log.info(String.format("Loaded %s rows from %s", recordsLoaded, this.fileName));
        } catch (Throwable e) {
            log.error(String.format("Failed to load CSV data from %s", this.fileName), e);
        }

        return recordsLoaded;
    }

    private void insertCsvRecord(CSVRecord record, WriteBatch batch) {
        recordWrapper.setRecord(record);
        String documentId = getCsvDocumentId(recordWrapper);
        Map<String, Object> docData = getDocumentData(recordWrapper);
        batch.set(getCollection().document(documentId), docData);
    }

    private String getCsvDocumentId(IRecord record) {
        List<String> values = new ArrayList<>();
        for (String key : tableKey.getKeys()) {
            values.add(record.getValue(key).toString());
        }
        return String.join("_", values);
    }


    private Map<String, Object> getDocumentData(IRecord record) {
        Map<String, Object> docData = new HashMap<>();

        for (String col : record.getColumnNames()) {
            docData.put(col, record.getValue(col));
        }

        return docData;
    }

    protected CollectionReference getCollection(){
        return getDb().collection(tableName);
    }

    protected Firestore getDb(){
        return firebaseConnectionFactory.getConnection();
    }

}
