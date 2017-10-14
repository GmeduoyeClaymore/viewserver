package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.Column;
import io.viewserver.datasource.ColumnType;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.Schema;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
        ShoppingCartDataSource {
    public static final String NAME = "shoppingCartItem";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new Schema()
                        .withColumns(Arrays.asList(
                                new Column("C_ShoppingCartId", "C_ShoppingCartId", ColumnType.Int),
                                new Column("C_CustomerId", "C_CustomerId", ColumnType.Int),
                                new Column("C_ProductId", "C_ProductId", ColumnType.Int),
                                new Column("C_ProductQuantity", "C_ProductQuantity", ColumnType.Int),
                                new Column("C_ShoppingCartCreationDate", "C_ShoppingCartCreationDate", ColumnType.DateTime)

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
