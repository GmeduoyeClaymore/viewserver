package io.viewserver.server.steps;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.shotgun.viewserver.setup.FirebaseApplicationSetup;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseUtils;
import io.viewserver.datasource.DataSourceRegistry;
import io.viewserver.server.setup.IApplicationGraphDefinitions;

public class FirebaseTestApplicationSetup  extends FirebaseApplicationSetup {
    public FirebaseTestApplicationSetup(FirebaseConnectionFactory connectionFactory, IApplicationGraphDefinitions graphDefinitions) {
        super(connectionFactory, graphDefinitions);
    }

    @Override
    protected void setup(Firestore db) {
        super.setup(db);
        delete(db, UserDataSource.NAME);
        delete(db, ContentTypeDataSource.NAME);
        delete(db, DeliveryDataSource.NAME);
        delete(db, DeliveryAddressDataSource.NAME);
        delete(db, OrderDataSource.NAME);
        delete(db, OrderItemsDataSource.NAME);
        delete(db, PhoneNumberDataSource.NAME);
        delete(db, ProductDataSource.NAME);
        delete(db, ProductCategoryDataSource.NAME);
        delete(db, VehicleDataSource.NAME);
    }

    private void delete(Firestore db, String name) {
        CollectionReference collection = db.collection(name);
        FirebaseUtils.deleteCollection(collection);
    }
}
