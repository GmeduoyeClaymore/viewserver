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
                                new Column("C_ID", "C_ID", ColumnType.String),
                                new Column("C_fname", "C_fname", ColumnType.String),
                                new Column("C_lname", "C_lname", ColumnType.String),
                                new Column("C_contactNo", "C_contactNo", ColumnType.String),
                                new Column("C_EmailAddress", "C_EmailAddress", ColumnType.String),
                                new Column("C_primaryDeliveryId", "C_primaryDeliveryId", ColumnType.String),
                                new Column("C_primaryPaymentId", "C_primaryPaymentId", ColumnType.String),
                                new Column("C_rating", "C_rating", ColumnType.Float)

                        ))
                )
                /*.withNodes(
                        new CalcColNode("fxRatesDayCalCol")
                                .withCalculations(new CalcColOperator.CalculatedColumn("day", "businessDay(date, false)- " + CsvDataSource.START_DATE_OFFSET))
                                .withCalculations(new CalcColOperator.CalculatedColumn("actualDay", "weekday(date)- " + CsvDataSource.START_DATE_OFFSET))
                                .withConnection(FxRatesDataSource.NAME),
                        new FilterNode("fxRatesFilter")
                                .withExpression("actualDay<6")
                                .withConnection("fxRatesDayCalCol")

                )
                .withDistributionMode(DistributionMode.Mirrored)
                .withOutput("fxRatesFilter")
                .withOptions(DataSourceOption.IsReportSource)*/;
    }
}
