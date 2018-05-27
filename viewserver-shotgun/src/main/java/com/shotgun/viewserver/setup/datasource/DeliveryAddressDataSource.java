package com.shotgun.viewserver.setup.datasource;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 27/09/17.
 */
public class DeliveryAddressDataSource {
    public static final String NAME = "deliveryAddress";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("deliveryAddressId", ContentType.String),
                                        new Column("userId", ContentType.String),
                                        new Column("created", ContentType.DateTime),
                                        new Column("lastUsed", ContentType.DateTime),
                                        new Column("isDefault", ContentType.Bool),
                                        new Column("flatNumber", ContentType.String),
                                        new Column("line1",  ContentType.String),
                                        new Column("line2",  ContentType.String),
                                        new Column("city", ContentType.String),
                                        new Column("country",  ContentType.String),
                                        new Column("postCode",  ContentType.String),
                                        new Column("latitude",  ContentType.Double),
                                        new Column("longitude", ContentType.Double),
                                        new Column("version", ContentType.Int),
                                        new Column("googlePlaceId",  ContentType.String)
                                        ))
                                .withKeyColumns("deliveryAddressId")
                )
                .withOutput(DataSource.TABLE_NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
