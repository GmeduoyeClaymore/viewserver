package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ColumnType;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.Schema;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class OrderItemsDataSource {
        public static final String NAME = "orderItems";

        public static DataSource getDataSource() {
                CsvDataAdapter dataAdapter = new CsvDataAdapter();
                dataAdapter.setFileName("data/orderItems.csv");
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
                                                new Column("orderId", "orderId", ColumnType.String),
                                                new Column("customerId", "customerId", ColumnType.String),
                                                new Column("productId", "productId", ColumnType.String),
                                                new Column("quantity", "quantity", ColumnType.Int)

                                        ))
                        );
        }
}
