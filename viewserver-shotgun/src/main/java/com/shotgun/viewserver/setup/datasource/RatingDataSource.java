package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class RatingDataSource {
    public static final String NAME = "rating";

    public static DataSource getDataSource(String firebaseKeyPath) {
        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                new FirebaseCsvDataAdapter(firebaseKeyPath, NAME, "data/rating.csv"),
                                null
                        )
                )
                .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("userId", "userId", ColumnType.String),
                                        new Column("orderId", "orderId", ColumnType.String),
                                        new Column("rating", "rating", ColumnType.Int)
                                ))
                                .withKeyColumns("orderId", "userId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
