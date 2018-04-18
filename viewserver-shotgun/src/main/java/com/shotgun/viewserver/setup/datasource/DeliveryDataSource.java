package com.shotgun.viewserver.setup.datasource;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
DeliveryDataSource {
    public static final String NAME = "delivery";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("deliveryId", ColumnType.String),
                                        new Column("customerId", ColumnType.String),
                                        new Column("created", ColumnType.DateTime),
                                        new Column("lastModified", ColumnType.DateTime),
                                        new Column("driverId", ColumnType.String),
                                        new Column("originDeliveryAddressId", ColumnType.String),
                                        new Column("destinationDeliveryAddressId", ColumnType.String),
                                        new Column("from",  ColumnType.DateTime),
                                        new Column("till",  ColumnType.DateTime),
                                        new Column("distance", ColumnType.Int),
                                        new Column("duration",  ColumnType.Int),
                                        new Column("fixedPriceValue",  ColumnType.Int),
                                        new Column("isFixedPrice",  ColumnType.Bool)
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
