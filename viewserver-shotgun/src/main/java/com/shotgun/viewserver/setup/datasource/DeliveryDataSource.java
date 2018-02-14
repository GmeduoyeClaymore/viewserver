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
                                        new Column("driverId", "driverId", ColumnType.String),
                                        new Column("originDeliveryAddressId", "originDeliveryAddressId", ColumnType.String),
                                        new Column("destinationDeliveryAddressId", "destinationDeliveryAddressId", ColumnType.String),
                                        new Column("from", "from", ColumnType.DateTime),
                                        new Column("till", "till", ColumnType.DateTime),
                                        new Column("driverRating", "driverRating", ColumnType.Int),
                                        new Column("customerRating", "customerRating", ColumnType.Int),
                                        new Column("distance", "distance", ColumnType.Int),
                                        new Column("duration", "duration", ColumnType.Int)
                                ))
                                .withKeyColumns("deliveryId")
                )
                .withOutput(NAME)
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_driverId", ColumnType.Int, "driverId")
                )
                .withDimensions(Arrays.asList(new Dimension("dimension_driverId", Cardinality.Int, ColumnType.String, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
