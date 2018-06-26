package com.shotgun.viewserver.setup;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoTestApplicationSetup  extends MongoApplicationSetup {
    private static final Logger log = LoggerFactory.getLogger(MongoTestApplicationSetup.class);
    public MongoTestApplicationSetup(MongoConnectionFactory connectionFactory, String csvDataPath) {
        super(connectionFactory, csvDataPath);
    }

    @Override
    protected void setup(MongoDatabase db, boolean complete) {
        if(complete) {
            recreate(db, ProductDataSource.NAME, ProductDataSource.getDataSource().getSchema());
            recreate(db, ContentTypeDataSource.NAME, ContentTypeDataSource.getDataSource().getSchema());
            recreate(db, ProductCategoryDataSource.NAME, ProductCategoryDataSource.getDataSource().getSchema());
            recreate(db, PhoneNumberDataSource.NAME, PhoneNumberDataSource.getDataSource().getSchema());
        }

        delete(db, PaymentDataSource.NAME);
        delete(db, MessagesDataSource.NAME);
        delete(db, DeliveryAddressDataSource.NAME);
        delete(db, OrderDataSource.NAME);
        delete(db, VehicleDataSource.NAME);

        recreate(db, ClusterDataSource.NAME, ClusterDataSource.getDataSource().getSchema());
        recreate(db, UserDataSource.NAME, UserDataSource.getDataSource().getSchema());
    }

    private void delete(MongoDatabase db, String name) {
        log.info("Deleting collection " + name);
        try {
            MongoCollection<Document> collection = db.getCollection(name);
            collection.drop();
        }catch (Exception ex){
            log.error("Problem dropping collection - {}",name);
        }
    }
}
