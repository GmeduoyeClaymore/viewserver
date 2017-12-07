package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
        VehicleDataSource {
    public static final String NAME = "vehicle";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/vehicle.csv");
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
                                        new Column("vehicleId", "vehicleId", ColumnType.String),
                                        new Column("userId", "userId", ColumnType.String),
                                        new Column("registrationNumber", "registrationNumber", ColumnType.String),
                                        new Column("colour", "colour", ColumnType.String),
                                        new Column("make", "make", ColumnType.String),
                                        new Column("model", "model", ColumnType.String),
                                        new Column("vehicleTypeId", "vehicleTypeId", ColumnType.String)
                                ))
                                .withKeyColumns("vehicleId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}