package com.shotgun.viewserver.setup.loaders;

import io.viewserver.adapters.common.sql.SimpleSqlDataQueryProvider;
import io.viewserver.adapters.jdbc.JdbcConnectionFactory;
import io.viewserver.adapters.jdbc.JdbcRecordLoader;
import io.viewserver.datasource.*;
import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.adapters.csv.CsvRecordLoader;
import io.viewserver.report.ReportRegistry;

import java.util.HashMap;
import java.util.Map;

public class CsvRecordLoaderCollection implements IRecordLoaderCollection {

    private HashMap<String,IRecordLoader> loaders;
    private String dataPath;

    public CsvRecordLoaderCollection(String dataPath) {
        this.dataPath = dataPath;
        loaders = new HashMap<>();
        register(UserDataSource.getDataSource().getSchema(), UserDataSource.NAME);
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
        loaders.put( getOperatorPath(operatorName),new CsvRecordLoader(schema, new OperatorCreationConfig(CreationStrategy.WAIT,CreationStrategy.WAIT)).withFileName(String.format("%s/%s.csv", dataPath,operatorName)));
    }

    static String getOperatorPath(String operatorName) {
        return String.format("/%s/%s/table", "datasources", operatorName);
    }


    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        return loaders;
    }
}



