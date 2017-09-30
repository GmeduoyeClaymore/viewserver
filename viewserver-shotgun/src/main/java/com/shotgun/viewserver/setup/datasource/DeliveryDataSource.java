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
DeliveryDataSource {
    public static final String NAME = "delivery";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/delivery.csv");
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
                                new Column("delivery_ID", "delivery_ID", ColumnType.String),
                                new Column("delivery_createdDateTime", "delivery_createdDateTime", ColumnType.DateTime),
                                new Column("delivery_Status", "delivery_Status", ColumnType.String),
                                new Column("D_ID", "D_ID", ColumnType.String),
                                new Column("orderFulfillment_ID", "orderFulfillment_ID", ColumnType.String),
                                new Column("delivery_size", "delivery_size", ColumnType.String),
                                new Column("customer_deliveryAddressID", "customer_deliveryAddressID", ColumnType.String),
                                new Column("C_ID", "C_ID", ColumnType.String),
                                new Column("M_ID", "M_ID", ColumnType.String),
                                new Column("delivery_targettedTime", "delivery_targettedTime", ColumnType.String),
                                new Column("delivery_shotgunDeliveryRequest", "delivery_shotgunDeliveryRequest", ColumnType.String),
                                new Column("delivery_shotgunDeliveryCarryIn", "delivery_shotgunDeliveryCarryIn", ColumnType.String),
                                new Column("delivery_shotgunDeliveryRoadside", "delivery_shotgunDeliveryRoadside", ColumnType.String),
                                new Column("delivery_requestedETA", "delivery_requestedETA", ColumnType.String)

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
