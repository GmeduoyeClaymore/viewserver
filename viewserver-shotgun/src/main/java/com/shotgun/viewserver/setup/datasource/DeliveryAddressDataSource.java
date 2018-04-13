package com.shotgun.viewserver.setup.datasource;


import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseConnectionFactory;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 27/09/17.
 */
public class DeliveryAddressDataSource {
    public static final String NAME = "deliveryAddress";

    public static DataSource getDataSource(IShotgunViewServerConfiguration shotgunConfiguration) {
        return new DataSource()
                .withName(NAME)
                .withDataLoader(DataSourceUtils.getDataLoader(shotgunConfiguration, NAME, "data/deliveryAddress.csv"))
                .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("deliveryAddressId", "deliveryAddressId", ColumnType.String),
                                        new Column("userId", "userId", ColumnType.String),
                                        new Column("created", "created", ColumnType.DateTime),
                                        new Column("lastUsed", "lastUsed", ColumnType.DateTime),
                                        new Column("isDefault", "isDefault", ColumnType.Bool),
                                        new Column("flatNumber", "flatNumber", ColumnType.String),
                                        new Column("line1", "line1", ColumnType.String),
                                        new Column("line2", "line2", ColumnType.String),
                                        new Column("city", "city", ColumnType.String),
                                        new Column("country", "country", ColumnType.String),
                                        new Column("postCode", "postCode", ColumnType.String),
                                        new Column("latitude", "latitude", ColumnType.Double),
                                        new Column("longitude", "longitude", ColumnType.Double),
                                        new Column("googlePlaceId", "googlePlaceId", ColumnType.String)
                                        ))
                                .withKeyColumns("deliveryAddressId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
