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
ProductDataSource {
    public static final String NAME = "product";

    public static DataSource getDataSource(String firebaseKeyPath) {
        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("productId", "productId", ColumnType.String),
                        new Column("name", "name", ColumnType.String),
                        new Column("description", "description", ColumnType.String),
                        new Column("categoryId", "categoryId", ColumnType.String),
                        new Column("price", "price", ColumnType.Double),
                        new Column("imageUrl", "imageUrl", ColumnType.String)
                ))
                .withKeyColumns("productId");

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                DataSourceUtils.get(firebaseKeyPath, NAME, "data/product.csv"),
                                null
                        )
                )
                .withSchema(schema)
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_productId", ColumnType.String, "productId"),
                        new CalculatedColumn("prodConstantJoinCol", ColumnType.Int, "1")
                )
                .withOutput(NAME)
                .withDimensions(Arrays.asList(new Dimension("dimension_productId", Cardinality.Int, ColumnType.String, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
