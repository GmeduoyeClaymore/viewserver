package io.viewserver.adapters.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

public class MongoConnectionFactory {
    private String clientURI;
    private String databaseName;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoConnectionFactory(String clientURI, String databaseName) {
        this.clientURI = clientURI;
        this.databaseName = databaseName;
    }

    private void createConnection() {
        try {
            CodecRegistry defaultCodecRegistry = MongoClient.getDefaultCodecRegistry();
            MongoCodecProvider myCodecProvider = new MongoCodecProvider();
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromProviders(myCodecProvider), defaultCodecRegistry);;
            mongoClient = new MongoClient( new MongoClientURI(clientURI, new MongoClientOptions.Builder().codecRegistry(codecRegistry)));
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
        if(mongoClient != null) {
            mongoClient.close();
        }
    }
}
