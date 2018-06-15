package com.shotgun.viewserver.setup;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoCsvDataLoader;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.server.setup.IApplicationSetup;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoApplicationSetup implements IApplicationSetup {

    private String csvDataPath;
    private static final Logger log = LoggerFactory.getLogger(MongoApplicationSetup.class);
    private MongoConnectionFactory connectionFactory;

    public MongoApplicationSetup(MongoConnectionFactory connectionFactory, String csvDataPath) {
        this.connectionFactory = connectionFactory;
        this.csvDataPath = csvDataPath;
    }

    @Override
    public void run(boolean complete) {
        log.info("Bootstrapping mongo database");
        setup(connectionFactory.getConnection(), complete);
    }


    protected void setup(MongoDatabase db,boolean complete) {
        if(complete) {
            recreate(db, ProductDataSource.NAME, ProductDataSource.getDataSource().getSchema());
            recreate(db, ContentTypeDataSource.NAME, ContentTypeDataSource.getDataSource().getSchema());
            recreate(db, ProductCategoryDataSource.NAME, ProductCategoryDataSource.getDataSource().getSchema());
            recreate(db, PhoneNumberDataSource.NAME, PhoneNumberDataSource.getDataSource().getSchema());
        }

        recreate(db, ClusterDataSource.NAME, ClusterDataSource.getDataSource().getSchema());
        delete(db, UserDataSource.NAME);
        delete(db, PaymentDataSource.NAME);
        delete(db, MessagesDataSource.NAME);

        delete(db, DeliveryAddressDataSource.NAME);
        delete(db, OrderDataSource.NAME);

        delete(db, VehicleDataSource.NAME);
    }

    private void delete(MongoDatabase db, String name) {
        try {
            log.info("Deleting collection " + name);
            MongoCollection<Document> collection = db.getCollection(name);
            collection.drop();
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
    }

    protected void recreate(MongoDatabase db, String operatorName, SchemaConfig schema) {
        delete(db,operatorName);
        MongoCsvDataLoader loader = new MongoCsvDataLoader(String.format("%s/%s.csv", csvDataPath,operatorName), operatorName, schema,connectionFactory);
        int noRecords = loader.load();
        log.info("{} records loading into mongo table {}",noRecords,operatorName);
    }

}


