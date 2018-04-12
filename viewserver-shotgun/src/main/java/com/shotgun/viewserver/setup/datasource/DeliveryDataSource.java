package com.shotgun.viewserver.setup.datasource;

import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
DeliveryDataSource {
    public static final String NAME = "delivery";

    public static DataSource getDataSource(IShotgunViewServerConfiguration shotgunConfiguration) {
        return new DataSource()
                .withName(NAME)
                .withDataLoader(DataSourceUtils.getDataLoader(shotgunConfiguration, NAME, "data/delivery.csv"))
                .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("deliveryId", "deliveryId", ColumnType.String),
                                        new Column("customerId", "customerId", ColumnType.String),
                                        new Column("created", "created", ColumnType.DateTime),
                                        new Column("lastModified", "lastModified", ColumnType.DateTime),
                                        new Column("driverId", "driverId", ColumnType.String),
                                        new Column("originDeliveryAddressId", "originDeliveryAddressId", ColumnType.String),
                                        new Column("destinationDeliveryAddressId", "destinationDeliveryAddressId", ColumnType.String),
                                        new Column("from", "from", ColumnType.DateTime),
                                        new Column("till", "till", ColumnType.DateTime),
                                        new Column("distance", "distance", ColumnType.Int),
                                        new Column("duration", "duration", ColumnType.Int),
                                        new Column("fixedPriceValue", "fixedPriceValue", ColumnType.Int),
                                        new Column("isFixedPrice", "isFixedPrice", ColumnType.Bool)
                                ))
                                .withKeyColumns("deliveryId")
                )
                .withOutput(NAME)
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_driverId", ColumnType.Int, "driverId")
                )
                .withDimensions(Arrays.asList(new Dimension("dimension_driverId", Cardinality.Int, ColumnType.String, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
