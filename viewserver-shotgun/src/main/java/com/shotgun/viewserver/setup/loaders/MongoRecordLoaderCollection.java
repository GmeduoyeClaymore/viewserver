package com.shotgun.viewserver.setup.loaders;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.mongo.MongoConnectionFactory;
import io.viewserver.adapters.mongo.MongoRecordLoader;
import io.viewserver.datasource.*;

import java.util.HashMap;
import java.util.Map;

public class MongoRecordLoaderCollection implements IRecordLoaderCollection {

    private HashMap<String,IRecordLoader> loaders;
    private MongoConnectionFactory connectionFactory;
    private String serverName;

    public MongoRecordLoaderCollection(MongoConnectionFactory connectionFactory, String serverName){
        this.connectionFactory = connectionFactory;
        this.serverName = serverName;
        loaders = new HashMap<>();
        register(OrderDataSource.getDataSource().getSchema(), OrderDataSource.NAME);
        register(ContentTypeDataSource.getDataSource().getSchema(), ContentTypeDataSource.NAME);
        register(DeliveryAddressDataSource.getDataSource().getSchema(), DeliveryAddressDataSource.NAME);
        register(PaymentDataSource.getDataSource().getSchema(), PaymentDataSource.NAME);
        register(PhoneNumberDataSource.getDataSource().getSchema(), PhoneNumberDataSource.NAME);
        register(ProductCategoryDataSource.getDataSource().getSchema(), ProductCategoryDataSource.NAME);
        register(ProductDataSource.getDataSource().getSchema(), ProductDataSource.NAME);
        register(UserDataSource.getDataSource().getSchema(), UserDataSource.NAME);
        register(MessagesDataSource.getDataSource().getSchema(), MessagesDataSource.NAME);
        register(VehicleDataSource.getDataSource().getSchema(), VehicleDataSource.NAME);
        register(ClusterDataSource.getDataSource().getSchema(), ClusterDataSource.NAME);
        registerAtRoot(DataSourceRegistry.getSchemaConfig(), IDataSourceRegistry.TABLE_NAME);
    }

    private void registerAtRoot(SchemaConfig schema, String operatorName) {
        loaders.put("/"+operatorName, getLoader(schema, operatorName));
    }

    private void register(SchemaConfig schema, String operatorName) {
        loaders.put(getOperatorPath(operatorName), getLoader(schema, operatorName));
    }

    private MongoRecordLoader getLoader(SchemaConfig schema, String operatorName) {
        return new MongoRecordLoader(connectionFactory, operatorName, schema, new OperatorCreationConfig(CreationStrategy.WAIT,CreationStrategy.WAIT), serverName);
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
        connectionFactory.close();
        loaders.values().forEach(c-> c.safeClose());
    }
}
