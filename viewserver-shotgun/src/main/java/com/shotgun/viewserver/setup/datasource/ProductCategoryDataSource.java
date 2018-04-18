package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class ProductCategoryDataSource {
    public static final String NAME = "productCategory";

    public static DataSource getDataSource() {
        SchemaConfig schema = new SchemaConfig()
                .withColumns(Arrays.asList(
                        new Column("categoryId", ColumnType.String),
                        new Column("category", ColumnType.String),
                        new Column("parentCategoryId", ColumnType.String),
                        new Column("path", ColumnType.String),
                        new Column("level", ColumnType.Int),
                        new Column("isLeaf", ColumnType.Bool)
                ))
                .withKeyColumns("categoryId");

        return new DataSource()
                .withName(NAME)
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_parentCategoryId","parentCategoryId",Cardinality.Byte, ColumnType.String)
                ))
                .withSchema(schema)
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
