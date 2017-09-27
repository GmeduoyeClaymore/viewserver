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
MerchantDataSource {
    public static final String NAME = "merchant";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/merchant.csv");
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
                                new Column("M_ID", "M_ID", ColumnType.String),
                                new Column("M_store", "M_store", ColumnType.String),
                                new Column("M_name", "M_name", ColumnType.String),
                                new Column("M_contactNo", "M_contactNo", ColumnType.String),
                                new Column("M_address", "M_address", ColumnType.String),
                                new Column("M_paymentAccount", "M_paymentAccount", ColumnType.String),
                                new Column("M_activeStatus", "M_activeStatus", ColumnType.String)

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
