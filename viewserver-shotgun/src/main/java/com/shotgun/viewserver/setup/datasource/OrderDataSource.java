package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.operators.calccol.CalcColOperator;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
OrderDataSource {
    public static final String NAME = "order";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/order.csv");
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
                                new Column("created", "created", ColumnType.DateTime),
                                new Column("lastModified", "lastModified", ColumnType.DateTime),
                                new Column("status", "status", ColumnType.String),
                                new Column("customerId", "customerId", ColumnType.String),
                                new Column("orderFulfillmentId", "orderFulfillmentId", ColumnType.String),
                                new Column("deliveryId", "deliveryId", ColumnType.String),
                                new Column("deliverySizeRequirement", "deliverySizeRequirement", ColumnType.String)
                        ))
                );
    }
}