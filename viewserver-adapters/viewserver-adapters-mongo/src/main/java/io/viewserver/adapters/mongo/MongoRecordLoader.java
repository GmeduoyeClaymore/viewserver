package io.viewserver.adapters.mongo;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.Block;
import com.mongodb.client.ChangeStreamIterable;
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
import org.bson.Document;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;

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
    private BsonDocument resumeToken;
    private int connectionRetries;
    private int MAX_CONNECTION_RETRY_LIMIT = 1000;
    private long lastConnectionRetryTime = 0;
    private long connectionLostTime = 0;
    private boolean isClosed;

    public MongoRecordLoader(MongoConnectionFactory connectionFactory, String tableName, SchemaConfig config, OperatorCreationConfig creationConfig, String serverName) {
        this.connectionFactory = connectionFactory;
        this.recordsById = new HashMap<>();
        this.tableName = tableName;
        this.service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat(this.tableName+"-" + serverName+ "-%d").build()));
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
            scheduleAddMongoListener();
            mongoListenerAdded = true;
        }
        return recordStream;
    }

    public Observable readyObservable(){
        return ready.take(1);
    }

    private void scheduleAddMongoListener(){
        logger.info(String.format("SCHEDULING - Addition of snapshot listener for Mongo table %s", tableName));
        this.service.submit(() -> {
            actuallyAddMongoListener();
        });
    }

    private void actuallyAddMongoListener() {
        if(this.isClosed){
            return;
        }
        try {
            logger.info(String.format("EXECUTING - Addition of snapshot listener for Mongo table %s", tableName));
            Block<ChangeStreamDocument<Document>> block = t -> {
                resetConnectionRetryCounter();
                Document fullDocument = t.getFullDocument();
                this.resumeToken = t.getResumeToken();
                if(fullDocument == null){
                    UpdateDescription description = t.getUpdateDescription();
                    if (t.getOperationType().equals(OperationType.INVALIDATE)) {
                        logger.info(String.format("DOCUMENT UPDATE IS INVALIDATION BARFING"));
                        throw new RuntimeException("Collection invalidated");
                    }
                    if(t.getDocumentKey() != null) {
                        String id = t.getDocumentKey().getString("_id").getValue();
                        if(t.getOperationType().equals(OperationType.DELETE)){

                        }else{
                            receiveBsonDocument(id, description.getUpdatedFields());
                        }
                    }
                }else{
                    logger.info(String.format("GOT DOCUMENT IN UPDATE - %s -version %s - Addition of snapshot listener for Mongo table %s",fullDocument.getString("_id"),fullDocument.getInteger("version"), tableName));
                    if (t.getOperationType().equals(OperationType.INVALIDATE)) {
                        logger.info(String.format("DOCUMENT UPDATE IS INVALIDATION BARFING"));
                        throw new RuntimeException("Collection invalidated");
                    }
                    receiveDocument(fullDocument);
                }

            };
            if(resumeToken == null) {
                logger.info(String.format("GETTING SNAPSHOT - Addition of snapshot listener for Mongo table %s", tableName));
                getCollection().find().forEach((Block<Document>) document -> {
                    resetConnectionRetryCounter();
                    logger.info("GOT DOCUMENT IN SNAPSHOT - {} - Addition of snapshot listener for Mongo table {}", document.getString("_id"), document.getInteger("version"), tableName);
                    receiveDocument(document);
                });
            }
            ready.onNext(new Object());
            ChangeStreamIterable<Document> changeStreamDocuments;
            if(resumeToken !=null){
                changeStreamDocuments = getCollection().watch().resumeAfter(resumeToken);
            }
            else{
                changeStreamDocuments = getCollection().watch();
            }
            changeStreamDocuments.forEach(block);
        } catch (Exception ex) {
            if(isClosed){
                logger.info("Expected exception as loader is closed" + ex.getMessage());
                return;
            }
            logger.error("Error adding snapshot listener for Mongo table %s {}", tableName, ex);
            if(connectionLostTime == 0) {
                connectionLostTime = new Date().getTime();
            }
            if(connectionRetries < MAX_CONNECTION_RETRY_LIMIT){
                while (!isClosed){ //binary exponential back off for connection retry
                    long now = new Date().getTime();
                    long timeElapsedSinceLastRetry = now - lastConnectionRetryTime;
                    long millisShouldWait = connectionRetries * 1000;
                    if(timeElapsedSinceLastRetry >= millisShouldWait){
                        if(lastConnectionRetryTime == 0){
                            logger.info("Trying to reconnect straight away as this is the first time that we have tried");
                        }else{
                            logger.info(String.format("Trying to reconnect as %s millis have elapsed since last retry attempt. Was instructed to wait for %s millis",timeElapsedSinceLastRetry,millisShouldWait));
                        }
                        connectionRetries++;
                        lastConnectionRetryTime = now;
                        actuallyAddMongoListener();
                    }else{
                        try {
                            Thread.sleep(millisShouldWait);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            logger.error("Cannot reconnect after {} attempts. Connection has been lost since {}",connectionRetries, new DateTime(connectionLostTime));
            throw new RuntimeException("Error getting records", ex);
        }
    }

    private void resetConnectionRetryCounter() {
        connectionLostTime = 0;
        connectionRetries = 0;
        lastConnectionRetryTime = 0;
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
                if(logger.isDebugEnabled()){
                    logger.debug("{} received record {}", tableName, rec.asString());
                }
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
        try {
            logger.info("Shutting down record loader");
            this.isClosed = true;
            this.service.shutdown();
            connectionFactory.close();
        }catch (Exception ex){
            logger.info("Problem closing record loader " + ex.getMessage());
        }
    }
}


