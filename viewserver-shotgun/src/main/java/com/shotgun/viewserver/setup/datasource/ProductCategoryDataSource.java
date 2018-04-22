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
                        new Column("categoryId", ContentType.String),
                        new Column("category", ContentType.String),
                        new Column("parentCategoryId", ContentType.String),
                        new Column("path", ContentType.String),
                        new Column("level", ContentType.Int),
                        new Column("isLeaf", ContentType.Bool)
                ))
                .withKeyColumns("categoryId");

        return new DataSource()
                .withName(NAME)
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_parentCategoryId","parentCategoryId",Cardinality.Byte, ContentType.String)
                ))
                .withSchema(schema)
                .withOutput(DataSource.TABLE_NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
