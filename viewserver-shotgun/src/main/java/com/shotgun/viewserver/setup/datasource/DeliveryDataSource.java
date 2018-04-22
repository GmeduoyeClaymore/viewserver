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
                                        new Column("deliveryId", ContentType.String),
                                        new Column("customerId", ContentType.String),
                                        new Column("created", ContentType.DateTime),
                                        new Column("lastModified", ContentType.DateTime),
                                        new Column("driverId", ContentType.String),
                                        new Column("originDeliveryAddressId", ContentType.String),
                                        new Column("destinationDeliveryAddressId", ContentType.String),
                                        new Column("from",  ContentType.DateTime),
                                        new Column("till",  ContentType.DateTime),
                                        new Column("distance", ContentType.Int),
                                        new Column("duration",  ContentType.Int),
                                        new Column("fixedPriceValue",  ContentType.Int),
                                        new Column("isFixedPrice",  ContentType.Bool)
                                ))
                                .withKeyColumns("deliveryId")
                )
                .withOutput(DataSource.TABLE_NAME)
                .withDimensions(Arrays.asList(new Dimension("dimension_driverId", Cardinality.Int, ContentType.String, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
