package com.shotgun.viewserver.setup.loaders;

import io.viewserver.datasource.*;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseRecordLoader;

import java.util.HashMap;
import java.util.Map;

public class FireBaseRecordLoaderCollection implements IRecordLoaderCollection {

    private HashMap<String,IRecordLoader> loaders;
    private FirebaseConnectionFactory connectionFactory;

    public FireBaseRecordLoaderCollection(FirebaseConnectionFactory connectionFactory){
        this.connectionFactory = connectionFactory;
    }

    public FireBaseRecordLoaderCollection() {
        loaders = new HashMap<>();
        register(OrderDataSource.getDataSource().getSchema(), OrderDataSource.NAME);
        register(ContentTypeDataSource.getDataSource().getSchema(), ContentTypeDataSource.NAME);
        register(DeliveryAddressDataSource.getDataSource().getSchema(), DeliveryAddressDataSource.NAME);
        register(DeliveryDataSource.getDataSource().getSchema(), DeliveryDataSource.NAME);
        register(OrderItemsDataSource.getDataSource().getSchema(), OrderItemsDataSource.NAME);
        register(PhoneNumberDataSource.getDataSource().getSchema(), PhoneNumberDataSource.NAME);
        register(ProductCategoryDataSource.getDataSource().getSchema(), ProductCategoryDataSource.NAME);
        register(ProductDataSource.getDataSource().getSchema(), ProductDataSource.NAME);
        register(RatingDataSource.getDataSource().getSchema(), RatingDataSource.NAME);
        register(UserRelationshipDataSource.getDataSource().getSchema(), UserRelationshipDataSource.NAME);
        register(VehicleDataSource.getDataSource().getSchema(), VehicleDataSource.NAME);
    }

    private void register(SchemaConfig schema, String operatorName) {
        loaders.put(getOperatorPath(operatorName),new FirebaseRecordLoader(connectionFactory, operatorName, schema, new OperatorCreationConfig(CreationStrategy.WAIT,CreationStrategy.WAIT)));
    }

    static String getOperatorPath(String operatorName) {
        return String.format("/%s/%s/table", "datasources/", operatorName);
    }


    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        return null;
    }
}
