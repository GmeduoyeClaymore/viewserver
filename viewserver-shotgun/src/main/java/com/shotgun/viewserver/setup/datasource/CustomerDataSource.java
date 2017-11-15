package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.operators.calccol.CalcColOperator;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
CustomerDataSource {
    public static final String NAME = "customer";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/customer.csv");
        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                dataAdapter,
                                null
                        )
                )
                .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("customerId", "customerId", ColumnType.String),
                                        new Column("title", "title", ColumnType.String),
                                        new Column("firstName", "firstName", ColumnType.String),
                                        new Column("lastName", "lastName", ColumnType.String),
                                        new Column("password", "password", ColumnType.String),
                                        new Column("contactNo", "contactNo", ColumnType.String),
                                        new Column("email", "email", ColumnType.String)
                                ))
                                .withKeyColumns("customerId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
