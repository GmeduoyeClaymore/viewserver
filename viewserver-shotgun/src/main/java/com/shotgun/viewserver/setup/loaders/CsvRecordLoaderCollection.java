package com.shotgun.viewserver.setup.loaders;

import io.viewserver.datasource.IRecordLoaderCollection;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.csv.CsvRecordLoader;
import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.SchemaConfig;

import java.util.HashMap;
import java.util.Map;

public class CsvRecordLoaderCollection implements IRecordLoaderCollection {

    private HashMap<String,IRecordLoader> loaders;

    public CsvRecordLoaderCollection() {
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
        loaders.put(operatorName,new CsvRecordLoader(schema).withFileName(String.format("data/%s.csv", operatorName)));
    }


    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        return loaders;
    }
}
