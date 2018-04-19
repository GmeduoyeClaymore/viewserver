package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class RatingDataSource {
    public static final String NAME = "rating";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("userId", ContentType.String),
                                        new Column("orderId", ContentType.String),
                                        new Column("rating", ContentType.Int)
                                ))
                                .withKeyColumns("orderId", "userId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
