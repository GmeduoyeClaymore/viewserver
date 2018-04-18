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
                                        new Column("orderItemId",  ColumnType.String),
                                        new Column("contentTypeId",  ColumnType.Int),
                                        new Column("orderId", ColumnType.String),
                                        new Column("userId",  ColumnType.String),
                                        new Column("productId",  ColumnType.String),
                                        new Column("notes", ColumnType.String),
                                        new Column("imageUrl", ColumnType.String),
                                        new Column("quantity", ColumnType.Int)
                                ))
                                .withKeyColumns("orderItemId"))
                        .withOutput(NAME)
                        .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
        }
}
