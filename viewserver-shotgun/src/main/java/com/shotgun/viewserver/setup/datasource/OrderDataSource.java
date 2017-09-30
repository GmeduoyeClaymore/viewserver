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
OrderDataSource {
    public static final String NAME = "orderDataSource";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/orders.csv");
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
                                new Column("order_ID", "order_ID", ColumnType.String),
                                new Column("order_createdDateTime", "order_createdDateTime", ColumnType.DateTime),
                                new Column("order_LastModifiedDateTime", "order_LastModifiedDateTime", ColumnType.DateTime),
                                new Column("C_ID", "C_ID", ColumnType.String),
                                new Column("orderFulfillment_ID", "orderFulfillment_ID", ColumnType.String),
                                new Column("delivery_ID", "delivery_ID", ColumnType.String),
                                new Column("order_Status", "order_Status", ColumnType.String),
                                new Column("paymentId", "paymentId", ColumnType.String),
                                new Column("order_DeliverySizeRequirement", "order_DeliverySizeRequirement", ColumnType.String),
                                new Column("totalOrderNotional", "totalOrderNotional", ColumnType.String)
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
