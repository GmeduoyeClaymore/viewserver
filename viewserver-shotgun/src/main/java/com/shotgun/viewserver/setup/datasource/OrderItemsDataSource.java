package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class OrderItemsDataSource {
        public static final String NAME = "orderItem";

        public static DataSource getDataSource() {
                return new DataSource()
                        .withName(NAME)
                        .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("orderItemId",  ContentType.String),
                                        new Column("contentTypeId",  ContentType.Int),
                                        new Column("orderId", ContentType.String),
                                        new Column("userId",  ContentType.String),
                                        new Column("productId",  ContentType.String),
                                        new Column("notes", ContentType.String),
                                        new Column("imageUrl", ContentType.String),
                                        new Column("quantity", ContentType.Int)
                                ))
                                .withKeyColumns("orderItemId"))
                        .withOutput(NAME)
                        .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
        }
}
