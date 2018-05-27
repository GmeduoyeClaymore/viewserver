package io.viewserver.adapters.mongo;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.client.model.changestream.UpdateDescription;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.OperatorCreationConfig;
import io.viewserver.datasource.SchemaConfig;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

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
    private ReplaySubject ready = ReplaySubject.create(1);
    private boolean mongoListenerAdded;


    public MongoRecordLoader(MongoConnectionFactory connectionFactory, String tableName, SchemaConfig config, OperatorCreationConfig creationConfig) {
        this.connectionFactory = connectionFactory;
        this.recordsById = new HashMap<>();
        this.tableName = tableName;
        this.service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat(this.tableName+"-%d").build()));
        this.config = config;
        this.creationConfig = creationConfig;
        //updateObservable.debounce(5, TimeUnit.MILLISECONDS).subscribe(res -> this.sendRecords());
        updateObservable.subscribe(res -> this.sendRecords());
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

    public Observable readyObservable(){
        return ready.take(1);
    }

    private void addMongoListener(){
        CompletableFuture<Integer> ss = new CompletableFuture<>();
        logger.info(String.format("SCHEDULING - Addition of snapshot listener for Mongo table %s", tableName));
        this.service.submit(() -> {
            try {
                logger.info(String.format("EXECUTING - Addition of snapshot listener for Mongo table %s", tableName));
                Block<ChangeStreamDocument<Document>> block = t -> {
                    Document fullDocument = t.getFullDocument();
                    if(fullDocument == null){
                        UpdateDescription description = t.getUpdateDescription();
                        receiveBsonDocument(t.getDocumentKey().getString("_id").getValue(),description.getUpdatedFields());
                    }else{
                        logger.info(String.format("GOT DOCUMENT IN UPDATE - %s -version %s - Addition of snapshot listener for Mongo table %s",fullDocument.getString("_id"),fullDocument.getInteger("version"), tableName));
                        if (t.getOperationType().equals(OperationType.INVALIDATE)) {
                            return;
                        }
                        receiveDocument(fullDocument);
                    }

                };
                logger.info(String.format("GETTING SNAPSHOT - Addition of snapshot listener for Mongo table %s", tableName));
                getCollection().find().forEach((Block<Document>) document -> {
                    logger.info(String.format("GOT DOCUMENT IN SNAPSHOT - %s - Addition of snapshot listener for Mongo table %s",document.getString("_id"),document.getInteger("version"), tableName));
                    receiveDocument(document);
                });
                ready.onNext(null);
                getCollection().watch().forEach(block);
            } catch (Exception ex) {
                logger.error(String.format("Error adding snapshot listener for Mongo table %s {}", tableName), ex);
                throw new RuntimeException("Error getting records", ex);
            }
        });
    }

    private void receiveBsonDocument(String documentId,BsonDocument document) {
        synchronized (recordsById) {
            MongoDocumentChangeRecord changeRecord = recordsById.get(documentId);
            if (changeRecord == null) {
                changeRecord = new MongoDocumentChangeRecord(config, document);
                changeRecord.setId(documentId);
                recordsById.put(documentId, changeRecord);
            } else {
                changeRecord.setValuesFrom(document);
            }
        }
        updateObservable.onNext(null);
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


