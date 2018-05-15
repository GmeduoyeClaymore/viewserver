package com.shotgun.viewserver.setup.loaders;

import io.viewserver.datasource.*;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseRecordLoader;
import io.viewserver.report.ReportRegistry;

import java.util.HashMap;
import java.util.Map;

public class FireBaseRecordLoaderCollection implements IRecordLoaderCollection {

    private HashMap<String,IRecordLoader> loaders;
    private FirebaseConnectionFactory connectionFactory;

    public FireBaseRecordLoaderCollection(FirebaseConnectionFactory connectionFactory){
        this.connectionFactory = connectionFactory;
        loaders = new HashMap<>();
        register(OrderDataSource.getDataSource().getSchema(), OrderDataSource.NAME);
        register(ContentTypeDataSource.getDataSource().getSchema(), ContentTypeDataSource.NAME);
        //register(DeliveryAddressDataSource.getDataSource().getSchema(), DeliveryAddressDataSource.NAME);
        register(PhoneNumberDataSource.getDataSource().getSchema(), PhoneNumberDataSource.NAME);
        register(ProductCategoryDataSource.getDataSource().getSchema(), ProductCategoryDataSource.NAME);
        register(ProductDataSource.getDataSource().getSchema(), ProductDataSource.NAME);
        register(UserRelationshipDataSource.getDataSource().getSchema(), UserRelationshipDataSource.NAME);
        register(UserDataSource.getDataSource().getSchema(), UserDataSource.NAME);
        register(VehicleDataSource.getDataSource().getSchema(), VehicleDataSource.NAME);
        registerAtRoot(DataSourceRegistry.getSchemaConfig(), IDataSourceRegistry.TABLE_NAME);
        registerAtRoot(ReportRegistry.getSchemaConfig(), ReportRegistry.TABLE_NAME);
    }

    private void registerAtRoot(SchemaConfig schema, String operatorName) {
        loaders.put("/"+operatorName, getLoader(schema, operatorName));
    }

    private void register(SchemaConfig schema, String operatorName) {
        loaders.put(getOperatorPath(operatorName), getLoader(schema, operatorName));
    }

    private FirebaseRecordLoader getLoader(SchemaConfig schema, String operatorName) {
        return new FirebaseRecordLoader(connectionFactory, operatorName, schema, new OperatorCreationConfig(CreationStrategy.WAIT,CreationStrategy.WAIT));
    }

    static String getOperatorPath(String operatorName) {
        return String.format("/%s/%s/table", "datasources", operatorName);
    }


    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        return loaders;
    }

    @Override
    public void close(){
        loaders.values().forEach(c-> c.close());
    }
}
