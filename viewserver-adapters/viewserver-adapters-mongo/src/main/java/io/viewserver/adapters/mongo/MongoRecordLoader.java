package io.viewserver.adapters.mongo;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.OperatorCreationConfig;
import io.viewserver.datasource.SchemaConfig;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MongoRecordLoader implements IRecordLoader{
    private static final Logger logger = LoggerFactory.getLogger(MongoRecordLoader.class);
    private final MongoConnectionFactory connectionFactory;
    private String tableName;
    private SchemaConfig config;
    private OperatorCreationConfig creationConfig;
    private MongoCollection<Document> collection;
    private ListeningExecutorService service;
    private HashMap<String,MongoDocumentChangeRecord> recordsById;
    private PublishSubject updateObservable = PublishSubject.create();
    private PublishSubject recordStream = PublishSubject.create();
    private boolean mongoListenerAdded;


    public MongoRecordLoader(MongoConnectionFactory connectionFactory, String tableName, SchemaConfig config, OperatorCreationConfig creationConfig) {
        this.connectionFactory = connectionFactory;
        this.recordsById = new HashMap<>();
        this.tableName = tableName;
        this.service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat(this.tableName+"-%d").build()));
        this.config = config;
        this.creationConfig = creationConfig;
        updateObservable.debounce(5, TimeUnit.MILLISECONDS).subscribe(res -> this.sendRecords());
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
        if(!mongoListenerAdded){
            addMongoListener();
            mongoListenerAdded = true;
        }
        return recordStream;
    }


    private void addMongoListener(){
        CompletableFuture<Integer> ss = new CompletableFuture<>();
        this.service.submit(() -> {
            try {
                logger.info(String.format("Adding snapshot listener for Mongo table %s", tableName));
                Block<ChangeStreamDocument<Document>> block = t -> {
                    Document fullDocument = t.getFullDocument();
                    System.out.println("received document update: " + fullDocument.getString("_id"));
                    if (t.getOperationType().equals(OperationType.INVALIDATE)) {
                        return;
                    }
                    receiveDocument(fullDocument);
                };
                getCollection().find().forEach((Block<Document>) document -> {
                    System.out.println("received doucment in snapshot: " + document.getString("_id"));
                    receiveDocument(document);
                });
                getCollection().watch().forEach(block);
                return ss.get();
            } catch (Exception ex) {
                logger.error(String.format("Error adding snapshot listener for Mongo table %s {}", tableName), ex);
                throw new RuntimeException("Error getting records", ex);
            }
        });
    }

    private void receiveDocument(Document document) {
        String id = document.getString("_id");
        synchronized (recordsById) {
            MongoDocumentChangeRecord changeRecord = recordsById.get(id);
            if (changeRecord == null) {
                changeRecord = new MongoDocumentChangeRecord(config, document);
                recordsById.put(id, changeRecord);
            } else {
                changeRecord.setValuesFrom(document);
            }
        }
        updateObservable.onNext(null);
    }

    private void sendRecords() {
        synchronized (recordsById){
            for(IRecord rec : recordsById.values()){
                recordStream.onNext(rec);
            }
            recordsById.clear();
        }
    }

    protected MongoCollection<Document> getCollection(){
        if(this.collection == null){
            this.collection = getDb().getCollection(tableName);
        }
        return this.collection;
    }

    protected MongoDatabase getDb(){
        return connectionFactory.getConnection();
    }


    @Override
    public void close(){
        connectionFactory.close();
    }
}


