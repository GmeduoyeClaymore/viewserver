package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
        VehicleDataSource {
    public static final String NAME = "vehicle";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("vehicleId", ColumnType.String),
                                        new Column("userId", ColumnType.String),
                                        new Column("registrationNumber",  ColumnType.String),
                                        new Column("colour", ColumnType.String),
                                        new Column("make",ColumnType.String),
                                        new Column("model",  ColumnType.String),
                                        new Column("dimensions",  ColumnType.String),
                                        new Column("selectedProductIds",  ColumnType.String),
                                        new Column("numAvailableForOffload", ColumnType.Int),
                                        new Column("bodyStyle", ColumnType.String)
                                ))
                                .withKeyColumns("vehicleId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
