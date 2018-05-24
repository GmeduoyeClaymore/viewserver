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
    private static final Logger log = LoggerFactory.getLogger(FirebaseTestApplicationSetup.class);
    public MongoTestApplicationSetup(MongoConnectionFactory connectionFactory, IApplicationGraphDefinitions graphDefinitions, String csvDataPath) {
        super(connectionFactory, graphDefinitions, csvDataPath);
    }

    @Override
    protected void setup(MongoDatabase db) {
        delete(db, UserDataSource.NAME);
        delete(db, MessagesDataSource.NAME);
        delete(db, ContentTypeDataSource.NAME);
        delete(db, DeliveryAddressDataSource.NAME);
        delete(db, OrderDataSource.NAME);
        delete(db, PhoneNumberDataSource.NAME);
        delete(db, ProductDataSource.NAME);
        delete(db, ProductCategoryDataSource.NAME);
        delete(db, VehicleDataSource.NAME);
        super.setup(db);
    }

    private void delete(MongoDatabase db, String name) {
        log.info("Deleting collection " + name);
        MongoCollection<Document> collection = db.getCollection(name);
        collection.drop();
    }
}
