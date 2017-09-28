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
DriverDataSource {
    public static final String NAME = "driver";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/driver.csv");
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
                                new Column("D_ID", "D_ID", ColumnType.String),
                                new Column("D_fname", "D_fname", ColumnType.String),
                                new Column("D_lname", "D_lname", ColumnType.String),
                                new Column("D_emailAddress", "D_emailAddress", ColumnType.String),
                                new Column("D_driverAddress", "D_driverAddress", ColumnType.String),
                                new Column("D_contactNo", "D_contactNo", ColumnType.String),
                                new Column("D_paymentID", "D_paymentID", ColumnType.String),
                                new Column("D_activeStatus", "D_activeStatus", ColumnType.String),
                                new Column("D_rating", "D_rating", ColumnType.String),
                                new Column("vehicleColour", "vehicleColour", ColumnType.String),
                                new Column("vehicleCategoryType", "vehicleCategoryType", ColumnType.String),
                                new Column("vehicleRegistration", "vehicleRegistration", ColumnType.String),
                                new Column("driverGPSLocationCoordinates", "driverGPSLocationCoordinates", ColumnType.String)

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
