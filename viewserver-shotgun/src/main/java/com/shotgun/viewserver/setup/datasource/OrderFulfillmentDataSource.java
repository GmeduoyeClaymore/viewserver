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
OrderFulfillmentDataSource {
    public static final String NAME = "orderFulfillments";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/orderFulfillments.csv");
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
                                new Column("orderFulfillment_ID", "orderFulfillment_ID", ColumnType.String),
                                new Column("orderFulfillment_createdDateTime", "orderFulfillment_createdDateTime", ColumnType.DateTime),
                                new Column("orderFulfillment_lastModifiedDateTime", "orderFulfillment_lastModifiedDateTime", ColumnType.DateTime),
                                new Column("order_Id", "order_Id", ColumnType.String),
                                new Column("C_ID", "C_ID", ColumnType.String),
                                new Column("M_ID", "M_ID", ColumnType.String),
                                new Column("D_ID", "D_ID", ColumnType.String),
                                new Column("ComputedDeliveryETA", "ComputedDeliveryETA", ColumnType.String),
                                new Column("TotalOrderFulfillmentNotional", "TotalOrderFulfillmentNotional", ColumnType.String),
                                new Column("NumOffloadersRequired", "NumOffloadersRequired", ColumnType.String)
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
