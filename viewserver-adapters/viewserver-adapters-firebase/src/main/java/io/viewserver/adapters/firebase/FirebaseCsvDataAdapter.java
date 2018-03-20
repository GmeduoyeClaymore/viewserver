package io.viewserver.adapters.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import io.viewserver.adapters.csv.CsvRecordWrapper;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IRecord;
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
import java.util.function.Consumer;

public class FirebaseCsvDataAdapter extends FirebaseDataAdapter{
    private static final Logger log = LoggerFactory.getLogger(FirebaseCsvDataAdapter.class);
    private CsvRecordWrapper recordWrapper;
    private String fileName;

    public FirebaseCsvDataAdapter(String firebaseKeyPath, String tableName, String fileName) {
        super(firebaseKeyPath, tableName);
        this.fileName = fileName;
        this.recordWrapper = new CsvRecordWrapper(new DateTime(DateTimeZone.UTC));
    }

    @Override
    public int getRecords(String query, Consumer<IRecord> consumer) {
        loadRecordsFromCsv();
        return super.getRecords(query, consumer);
    }

    protected int loadRecordsFromCsv() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        int recordsLoaded = 0;
        try {
            UTF8StreamReader reader = new UTF8StreamReader();
            reader.setInput(stream);
            try {
                CSVParser parser = CSVFormat.EXCEL.withHeader().parse(reader);

                for (final CSVRecord record : parser) {
                    recordWrapper.setRecord(record);
                    insertCsvRecord(recordWrapper);
                    recordsLoaded++;
                }
            } finally {
                reader.close();
            }
            log.info(String.format("Loaded %s rows from %s", recordsLoaded, this.fileName));
        } catch (Throwable e) {
            log.error(String.format("Failed to load CSV data from %s", this.fileName), e);
        }

        return recordsLoaded;
    }

    private void insertCsvRecord(IRecord record) {
        String documentId = getCsvDocumentId(record);
        Map<String, Object> docData = getDocumentData(record);
        try {
            ApiFuture<WriteResult> future = getCollection().document(documentId).set(docData);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

        //TODO - add result listeners here
    }

    private String getCsvDocumentId(IRecord record){
        List<String> values = new ArrayList<>();

        for(String key: tableKeyDefinition.getKeys()){
            values.add(record.getValue(key).toString());
        }

        return String.join("_", values);
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        recordWrapper.setDataSource(dataSource);
    }

    private Map<String, Object> getDocumentData(IRecord record){
        Map<String, Object> docData = new HashMap<>();

        for(Column col: dataSource.getSchema().getColumns()){
            docData.put(col.getName(), record.getValue(col.getName()));
        }

        return docData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
