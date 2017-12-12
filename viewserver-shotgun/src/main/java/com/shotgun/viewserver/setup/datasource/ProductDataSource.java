    package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
ProductDataSource {
    public static final String NAME = "product";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/product.csv");
        dataAdapter.setMultiple(5000);
        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("productId", "productId", ColumnType.String),
                        new Column("name", "name", ColumnType.String),
                        new Column("description", "description", ColumnType.String),
                        new Column("categoryId", "categoryId", ColumnType.String),
                        new Column("price", "price", ColumnType.Int),
                        new Column("rating", "rating", ColumnType.Int)
                ))
                .withKeyColumns("productId");

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                dataAdapter,
                                null
                        )
                )
                .withSchema(schema)
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
