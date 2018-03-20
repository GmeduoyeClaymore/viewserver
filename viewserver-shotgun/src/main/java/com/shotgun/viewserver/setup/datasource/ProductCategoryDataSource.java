package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.adapters.firebase.FirebaseCsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.GroupByNode;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
ProductCategoryDataSource {
    public static final String NAME = "productCategory";

    public static DataSource getDataSource(String firebaseKeyPath) {
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
                                new FirebaseCsvDataAdapter(firebaseKeyPath, NAME, "data/productCategory.csv"),
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
