package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
        VehicleDataSource {
    public static final String NAME = "vehicle";

    public static DataSource getDataSource(String firebaseKeyPath) {
        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                DataSourceUtils.get(firebaseKeyPath, NAME, "data/vehicle.csv"),
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
                                        new Column("dimensions", "dimensions", ColumnType.String),
                                        new Column("selectedProductIds", "selectedProductIds", ColumnType.String),
                                        new Column("numAvailableForOffload", "numAvailableForOffload", ColumnType.Int),
                                        new Column("bodyStyle", "bodyStyle", ColumnType.String)
                                ))
                                .withKeyColumns("vehicleId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
