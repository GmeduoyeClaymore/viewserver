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
                        new Column("productId", ContentType.String),
                        new Column("name", ContentType.String),
                        new Column("description", ContentType.String),
                        new Column("categoryId", ContentType.String),
                        new Column("price", ContentType.Int),
                        new Column("imageUrl", ContentType.String)
                ))
                .withKeyColumns("productId");

        return new DataSource()
                .withName(NAME)
                .withSchema(schema)
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_productId", ContentType.String, "productId"),
                        new CalculatedColumn("prodConstantJoinCol", ContentType.Int, "1")
                )
                .withOutput(NAME)
                .withDimensions(Arrays.asList(new Dimension("dimension_productId", Cardinality.Int, ContentType.String, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
