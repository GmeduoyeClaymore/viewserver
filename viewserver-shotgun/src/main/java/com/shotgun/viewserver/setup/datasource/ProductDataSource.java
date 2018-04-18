    package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
ProductDataSource {
    public static final String NAME = "product";

    public static DataSource getDataSource() {
        SchemaConfig schema = new SchemaConfig()
                .withColumns(Arrays.asList(
                        new Column("productId", ColumnType.String),
                        new Column("name", ColumnType.String),
                        new Column("description", ColumnType.String),
                        new Column("categoryId", ColumnType.String),
                        new Column("price", ColumnType.Int),
                        new Column("imageUrl", ColumnType.String)
                ))
                .withKeyColumns("productId");

        return new DataSource()
                .withName(NAME)
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
