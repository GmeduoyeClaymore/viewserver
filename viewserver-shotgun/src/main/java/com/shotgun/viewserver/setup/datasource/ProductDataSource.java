package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
ProductDataSource {
    public static final String NAME = "product";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/products.csv");
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
                                new Column("P_ID", "P_ID", ColumnType.String),
                                new Column("P_name", "P_name", ColumnType.String),
                                new Column("P_description", "P_description", ColumnType.String),
                                new Column("P_categoryType", "P_categoryType", ColumnType.String),
                                new Column("P_subCategoryType", "P_subCategoryType", ColumnType.String),
                                new Column("P_packageCode", "P_packageCode", ColumnType.String),
                                new Column("P_size", "P_size", ColumnType.String),
                                new Column("P_dimensions", "P_dimensions", ColumnType.String),
                                new Column("P_price", "P_price", ColumnType.String),
                                new Column("P_rating", "P_rating", ColumnType.String)
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
