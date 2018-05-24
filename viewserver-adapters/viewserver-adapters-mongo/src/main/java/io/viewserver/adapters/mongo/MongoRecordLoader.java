package io.viewserver.adapters.mongo;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import io.viewserver.datasource.IRecord;
import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.OperatorCreationConfig;
import io.viewserver.datasource.SchemaConfig;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MongoRecordLoader implements IRecordLoader{
    private static final Logger logger = LoggerFactory.getLogger(MongoRecordLoader.class);
    private final MongoConnectionFactory connectionFactory;
    private String tableName;
    private SchemaConfig config;
    private OperatorCreationConfig creationConfig;
    private MongoCollection<Document> collection;
    private ListeningExecutorService service;

    public MongoRecordLoader(MongoConnectionFactory connectionFactory, String tableName, SchemaConfig config, OperatorCreationConfig creationConfig) {
        this.connectionFactory = connectionFactory;
        this.tableName = tableName;
        this.service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat(this.tableName+"-%d").build()));
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
                this.service.submit(() -> addMongoListener(rec -> subscriber.onNext(rec)));
            }catch (Exception ex){
                subscriber.onError(ex);
            }}, Emitter.BackpressureMode.BUFFER);

    }


    private int addMongoListener(Consumer<IRecord> consumer){
        CompletableFuture<Integer> ss = new CompletableFuture<>();

        try {
            logger.info(String.format("Adding snapshot listener for Mongo table %s", tableName));
            Block<ChangeStreamDocument<Document>> block = new Block<ChangeStreamDocument<Document>>() {
                @Override
                public void apply(ChangeStreamDocument<Document> t) {
                    System.out.println("received update: " + t.getFullDocument());
                    MongoDocumentChangeRecord changeRecord = new MongoDocumentChangeRecord(config, t.getFullDocument());
                    consumer.accept(changeRecord);
                }
            };
            getCollection().find().forEach(new Block<Document>() {
                @Override
                public void apply(Document document) {
                    System.out.println("received snapshot: " + document);
                    MongoDocumentChangeRecord changeRecord = new MongoDocumentChangeRecord(config, document);
                    consumer.accept(changeRecord);
                }
            });
            getCollection().watch().forEach(block);
            /*getCollection().watch().forEach((Consumer<ChangeStreamDocument<Document>>) documentChangeStreamDocument -> {
                MongoDocumentChangeRecord changeRecord = new MongoDocumentChangeRecord(config, documentChangeStreamDocument);
                consumer.accept(changeRecord);
            });*/
            return ss.get();
        }catch(Exception ex){
            logger.error(String.format("Error adding snapshot listener for Mongo table %s {}", tableName),ex);
            throw new RuntimeException("Error getting records", ex);
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
    }
}


