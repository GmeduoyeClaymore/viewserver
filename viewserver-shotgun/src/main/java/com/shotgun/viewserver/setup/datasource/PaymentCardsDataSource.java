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
PaymentCardsDataSource {
    public static final String NAME = "paymentCards";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/paymentCards.csv");
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
                                        new Column("userId", "userId", ColumnType.String),
                                        new Column("paymentId", "paymentId", ColumnType.String),
                                        new Column("isDefault", "isDefault", ColumnType.Bool),
                                        new Column("cardNumber", "cardNumber", ColumnType.String),
                                        new Column("expiryDate", "expiryDate", ColumnType.String)
                                ))
                                .withKeyColumns("paymentId")
                )
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
