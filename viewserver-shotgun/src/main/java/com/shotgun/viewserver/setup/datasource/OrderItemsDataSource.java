package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.IndexNode;
import io.viewserver.execution.nodes.UnEnumNode;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class OrderItemsDataSource {
        public static final String NAME = "orderItem";

        public static DataSource getDataSource() {
                CsvDataAdapter dataAdapter = new CsvDataAdapter();
                dataAdapter.setFileName("data/orderItem.csv");
            Schema schema = new Schema()
                    .withColumns(Arrays.asList(
                            new Column("orderId", "orderId", ColumnType.String),
                            new Column("customerId", "customerId", ColumnType.String),
                            new Column("productId", "productId", ColumnType.String),
                            new Column("quantity", "quantity", ColumnType.Int)
                    ));
            return new DataSource()
                        .withName(NAME)
                        .withDataLoader(
                                new DataLoader(
                                        NAME,
                                        dataAdapter,
                                        null
                                )
                        )
                        .withSchema(schema)
                        .withOutput(NAME);
        }
}
