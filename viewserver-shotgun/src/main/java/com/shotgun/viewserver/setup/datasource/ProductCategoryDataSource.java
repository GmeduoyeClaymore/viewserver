package com.shotgun.viewserver.setup.datasource;


import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
ProductCategoryDataSource {
    public static final String NAME = "productCategory";

    public static DataSource getDataSource(IShotgunViewServerConfiguration shotgunConfiguration) {
        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("categoryId", "categoryId", ColumnType.String),
                        new Column("category", "category", ColumnType.String),
                        new Column("parentCategoryId", "parentCategoryId", ColumnType.String),
                        new Column("path", "path", ColumnType.String),
                        new Column("level", "level", ColumnType.Int),
                        new Column("isLeaf", "isLeaf", ColumnType.Bool)
                ))
                .withKeyColumns("categoryId");

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                DataSourceUtils.getCsvDataAdapter(shotgunConfiguration, NAME, "data/productCategory.csv", true),
                                null
                        )
                )
                .withCalculatedColumns(
                        new CalculatedColumn("dimension_parentCategoryId", ColumnType.String, "parentCategoryId")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_parentCategoryId", Cardinality.Byte, ColumnType.String)
                ))
                .withSchema(schema)
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
