package com.shotgun.viewserver.setup.datasource;


import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.GroupByNode;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class
ProductCategoryDataSource {
    public static final String NAME = "productCategory";

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/productCategory.csv");

//        Define Schema
        Schema schema = new Schema()
                .withColumns(Arrays.asList(
                        new Column("CategoryType", "CategoryType", ColumnType.String),
                        new Column("SubCategoryType", "SubCategoryType", ColumnType.String),
                        new Column("PackageCode", "PackageCode", ColumnType.String)
                ));

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                dataAdapter,
                                null
                        )
                )
                /*.withDimensions(Arrays.asList(

                        new Dimension("CategoryType", Cardinality.Int, schema.getColumn("CategoryType"))
                        .setLabel("Category Type").setPlural("Category Types").setGroup("CategoryType")
                ))*/
                .withSchema(schema)
                /*.withNodes(
                    new GroupByNode("prodCategories")
                        .withGroupByColumns("CategoryType")
                            .withConnection(NAME)
                )*/
//                .withDistributionMode(DistributionMode.Mirrored)
//                .withOutput("prodCategories")
                        /*new CalcColNode("fxRatesDayCalCol")
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
