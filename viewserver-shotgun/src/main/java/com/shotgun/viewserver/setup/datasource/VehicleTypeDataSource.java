package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
        VehicleTypeDataSource {
    public static final String NAME = "vehicleType";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/vehicleType.csv");
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
                                        new Column("vehicleTypeId", "vehicleTypeId", ColumnType.String),
                                        new Column("bodyType", "bodyType", ColumnType.String),
                                        new Column("capacity", "capacity", ColumnType.Int),
                                        new Column("payload", "payload", ColumnType.Int),
                                        new Column("description", "description", ColumnType.String),
                                        new Column("image", "image", ColumnType.String)
                                ))
                                .withKeyColumns("vehicleTypeId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
