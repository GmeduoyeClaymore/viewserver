package io.viewserver.adapters.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.viewserver.adapters.ILoader;
import io.viewserver.adapters.csv.CsvRecordWrapper;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.operators.table.TableKeyDefinition;
import javolution.io.UTF8StreamReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoCsvDataLoader implements ILoader {
    private static final Logger log = LoggerFactory.getLogger(MongoCsvDataLoader.class);
    private CsvRecordWrapper recordWrapper;
    private String fileName;
    private String tableName;
    private TableKeyDefinition tableKey;
    private MongoConnectionFactory firebaseConnectionFactory;

    public MongoCsvDataLoader(String fileName, String tableName, SchemaConfig config, MongoConnectionFactory firebaseConnectionFactory) {
        this.fileName = fileName;
        this.tableName = tableName;
        this.tableKey = config.getTableKeyDefinition();
        this.firebaseConnectionFactory = firebaseConnectionFactory;
        this.recordWrapper = new CsvRecordWrapper(new DateTime(DateTimeZone.UTC),config);
    }

    public int load() {
        List<? extends Document> documentsFromCSV = getDocumentsFromCSV();
        if(documentsFromCSV.size() > 0){
            getCollection().insertMany(documentsFromCSV);
        }else{
            getDb().createCollection(tableName);
        }
        log.info(String.format("Loaded %s rows from %s", documentsFromCSV.size(), this.fileName));
        return documentsFromCSV.size();
    }

    private List<Document> getDocumentsFromCSV() {
        List<Document> documents = new ArrayList<>();
        try {
            UTF8StreamReader reader = new UTF8StreamReader();
            InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
            reader.setInput(stream);
            CSVParser parser = CSVFormat.EXCEL.withHeader().parse(reader);

            for (final CSVRecord record : parser) {
                insertCsvRecord(record, documents);
            }
        }  catch (Throwable e) {
            log.error(String.format("Failed to load CSV data from %s", this.fileName), e);
        }
       return documents;

    }

    private void insertCsvRecord(CSVRecord record, List<Document> batch) {
        recordWrapper.setRecord(record);
        Map<String, Object> docData = getDocumentData(recordWrapper);
        batch.add(new Document(docData));
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
        docData.put("_id", getCsvDocumentId(recordWrapper));
        return docData;
    }

    protected MongoCollection<Document> getCollection(){
        return getDb().getCollection(tableName);
    }

    protected MongoDatabase getDb(){
        return firebaseConnectionFactory.getConnection();
    }

}
