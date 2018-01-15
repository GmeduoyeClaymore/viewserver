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
                                        new Column("userIdDelivery", "userIdDelivery", ColumnType.String),
                                        new Column("created", "created", ColumnType.DateTime),
                                        new Column("lastModified", "lastModified", ColumnType.DateTime),
                                        new Column("vehicleTypeId", "vehicleTypeId", ColumnType.String),
                                        new Column("driverId", "driverId", ColumnType.String),
                                        new Column("originDeliveryAddressId", "originDeliveryAddressId", ColumnType.String),
                                        new Column("destinationDeliveryAddressId", "destinationDeliveryAddressId", ColumnType.String),
                                        new Column("eta", "eta", ColumnType.DateTime),
                                        new Column("noRequiredForOffload", "noRequiredForOffload", ColumnType.Int),
                                        new Column("driverRating", "driverRating", ColumnType.Int),
                                        new Column("customerRating", "customerRating", ColumnType.Int)
                                ))
                                .withKeyColumns("deliveryId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
