package io.viewserver.adapters.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionFactory.class);

    private String clientURI;
    private String databaseName;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoConnectionFactory(String clientURI, String databaseName) {
        this.clientURI = clientURI;
        this.databaseName = databaseName;
    }

    private synchronized void createConnection() {
        try {
            if(database != null){
                return;
            }
            logger.info("Creating connection");
            CodecRegistry defaultCodecRegistry = MongoClient.getDefaultCodecRegistry();
            MongoCodecProvider myCodecProvider = new MongoCodecProvider();
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromProviders(myCodecProvider), defaultCodecRegistry);;
            mongoClient = new MongoClient( new MongoClientURI(clientURI, new MongoClientOptions.Builder().socketTimeout(Integer.MAX_VALUE).codecRegistry(codecRegistry)));
            database = mongoClient.getDatabase(databaseName);

        }catch(Exception ex){
            throw new RuntimeException("Could not open a connection to the mongo database", ex);
        }
    }

    @JsonIgnore
    public MongoDatabase getConnection(){
        if(database == null){
            createConnection();
        }
        return database;
    }

    public void close(){
        try{
            logger.info("Closing connection factory");
            if(mongoClient != null) {
                mongoClient.close();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
