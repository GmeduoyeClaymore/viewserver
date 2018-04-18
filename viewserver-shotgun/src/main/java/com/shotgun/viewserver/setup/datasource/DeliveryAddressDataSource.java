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
                                        new Column("deliveryAddressId", ColumnType.String),
                                        new Column("userId", ColumnType.String),
                                        new Column("created", ColumnType.DateTime),
                                        new Column("lastUsed", ColumnType.DateTime),
                                        new Column("isDefault", ColumnType.Bool),
                                        new Column("flatNumber", ColumnType.String),
                                        new Column("line1",  ColumnType.String),
                                        new Column("line2",  ColumnType.String),
                                        new Column("city", ColumnType.String),
                                        new Column("country",  ColumnType.String),
                                        new Column("postCode",  ColumnType.String),
                                        new Column("latitude",  ColumnType.Double),
                                        new Column("longitude", ColumnType.Double),
                                        new Column("googlePlaceId",  ColumnType.String)
                                        ))
                                .withKeyColumns("deliveryAddressId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
