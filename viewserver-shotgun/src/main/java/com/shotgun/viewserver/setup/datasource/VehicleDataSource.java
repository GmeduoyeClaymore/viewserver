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
                                        new Column("vehicleId", ContentType.String),
                                        new Column("userId", ContentType.String),
                                        new Column("registrationNumber",  ContentType.String),
                                        new Column("colour", ContentType.String),
                                        new Column("make", ContentType.String),
                                        new Column("model",  ContentType.String),
                                        new Column("dimensions",  ContentType.String),
                                        new Column("selectedProductIds",  ContentType.String),
                                        new Column("numAvailableForOffload", ContentType.Int),
                                        new Column("bodyStyle", ContentType.String)
                                ))
                                .withKeyColumns("vehicleId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
