package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class OrderItemsDataSource {
        public static final String NAME = "orderItem";

        public static DataSource getDataSource() {
                CsvDataAdapter dataAdapter = new CsvDataAdapter();
                dataAdapter.setFileName("data/orderItem.csv");

                return new DataSource()
                        .withName(NAME)
                        .withDataLoader(
                                new DataLoader(
                                        NAME,
                                        dataAdapter,
                                        null
                                )
                        )
                        .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("orderItemId", "orderItemId", ColumnType.String),
                                        new Column("orderId", "orderId", ColumnType.String),
                                        new Column("userId", "userId", ColumnType.String),
                                        new Column("productId", "productId", ColumnType.String),
                                        new Column("notes", "notes", ColumnType.String),
                                        new Column("imageUrl", "imageUrl", ColumnType.String),
                                        new Column("quantity", "quantity", ColumnType.Int)
                                ))
                                .withKeyColumns("orderItemId"))
                        .withOutput(NAME)
                        .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
        }
}
