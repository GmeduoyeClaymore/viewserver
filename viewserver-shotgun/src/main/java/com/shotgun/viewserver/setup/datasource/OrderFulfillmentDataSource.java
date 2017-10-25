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
OrderFulfillmentDataSource {
    public static final String NAME = "orderFulfillment";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/orderFulfillment.csv");
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
                                new Column("orderFulfillmentId", "orderFulfillmentId", ColumnType.String),
                                new Column("created", "created", ColumnType.DateTime),
                                new Column("modified", "modified", ColumnType.DateTime),
                                new Column("merchantId", "merchantId", ColumnType.String),
                                new Column("orderId", "orderId", ColumnType.String)
                        ))
                );
    }
}
