package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.GroupByNode;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
ProductCategoryDataSource {
    public static final String NAME = "productCategory";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/productCategory.csv");

        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("categoryId", "categoryId", ColumnType.String),
                        new Column("category", "category", ColumnType.String),
                        new Column("parentCategoryId", "parentCategoryId", ColumnType.String)
                ))
                .withKeyColumns("categoryId");

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
