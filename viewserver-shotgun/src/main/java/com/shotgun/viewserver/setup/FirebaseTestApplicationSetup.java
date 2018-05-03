package com.shotgun.viewserver.setup;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.server.setup.IApplicationGraphDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirebaseTestApplicationSetup  extends FirebaseApplicationSetup {
    private static final Logger log = LoggerFactory.getLogger(FirebaseTestApplicationSetup.class);
    public FirebaseTestApplicationSetup(FirebaseConnectionFactory connectionFactory, IApplicationGraphDefinitions graphDefinitions, String csvDataPath) {
        super(connectionFactory, graphDefinitions, csvDataPath);
    }

    @Override
    protected void setup(Firestore db) {
        delete(db, UserDataSource.NAME);
        delete(db, ContentTypeDataSource.NAME);
        delete(db, DeliveryAddressDataSource.NAME);
        delete(db, OrderDataSource.NAME);
        delete(db, PhoneNumberDataSource.NAME);
        delete(db, ProductDataSource.NAME);
        delete(db, ProductCategoryDataSource.NAME);
        delete(db, VehicleDataSource.NAME);
        delete(db, "reports");
        super.setup(db);
    }

    private void delete(Firestore db, String name) {
        log.info("Deleting collection " + name);
        CollectionReference collection = db.collection(name);
        FirebaseUtils.deleteCollection(collection);
    }
}
