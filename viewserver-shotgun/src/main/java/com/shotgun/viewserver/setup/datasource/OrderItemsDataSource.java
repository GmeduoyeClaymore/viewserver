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
public class OrderItemsDataSource {
        public static final String NAME = "orderItem";

        public static DataSource getDataSource(IShotgunViewServerConfiguration shotgunConfiguration) {
                return new DataSource()
                        .withName(NAME)
                        .withDataLoader(DataSourceUtils.getDataLoader(shotgunConfiguration, NAME, "data/orderItem.csv"))
                        .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("orderItemId", "orderItemId", ColumnType.String),
                                        new Column("contentTypeId", "contentTypeId", ColumnType.Int),
                                        new Column("orderId", "orderId", ColumnType.String),
                                        new Column("userId", "userId", ColumnType.String),
                                        new Column("productId", "productId", ColumnType.String),
                                        new Column("notes", "notes", ColumnType.String),
                                        new Column("imageUrl", "imageUrl", ColumnType.String),
                                        new Column("quantity", "quantity", ColumnType.Int),
                                        new Column("fixedPrice", "fixedPrice", ColumnType.Int),
                                        new Column("startTime", "startTime", ColumnType.DateTime),
                                        new Column("endTime", "endTime", ColumnType.DateTime)
                                ))
                                .withKeyColumns("orderItemId"))
                        .withOutput(NAME)
                        .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
        }
}
