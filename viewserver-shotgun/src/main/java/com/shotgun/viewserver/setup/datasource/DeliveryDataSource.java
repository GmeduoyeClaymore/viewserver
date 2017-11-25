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
DeliveryDataSource {
    public static final String NAME = "delivery";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/delivery.csv");
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
                                        new Column("deliveryId", "deliveryId", ColumnType.String),
                                        new Column("customerIdDelivery", "customerIdDelivery", ColumnType.String),
                                        new Column("deliveryAddressId", "deliveryAddressId", ColumnType.String),
                                        new Column("created", "created", ColumnType.DateTime),
                                        new Column("lastModified", "lastModified", ColumnType.DateTime),
                                        new Column("orderFulfillmentId", "orderFulfillmentId", ColumnType.String),
                                        new Column("driverId", "driverId", ColumnType.String),
                                        new Column("type", "type", ColumnType.String),
                                        new Column("eta", "eta", ColumnType.Int),
                                        new Column("noRequiredForOffload", "noRequiredForOffload", ColumnType.Int)
                                ))
                                .withKeyColumns("deliveryId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
