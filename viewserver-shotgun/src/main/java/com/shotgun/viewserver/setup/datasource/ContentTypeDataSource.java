package com.shotgun.viewserver.setup.datasource;

import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
import io.viewserver.datasource.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bennett on 26/09/17.
 */
//name	origin	destination	noPeople	fromTime	toTime	hasVehicle	rootProductCategory


public class
        ContentTypeDataSource {
    public static final String NAME = "contentType";

    public static final Schema schema = new Schema()
            .withColumns(Arrays.asList(
                    new Column("contentTypeId", "contentTypeId", ColumnType.Int),
                    new Column("name", "name", ColumnType.String),
                    new Column("origin", "origin", ColumnType.Bool),
                    new Column("destination", "destination", ColumnType.Bool),
                    new Column("noPeople", "noPeople", ColumnType.Bool),
                    new Column("fromTime", "fromTime", ColumnType.Bool),
                    new Column("tillTime", "tillTime", ColumnType.Bool),
                    new Column("noItems", "noItems", ColumnType.Bool),
                    new Column("rootProductCategory", "rootProductCategory", ColumnType.String),
                    new Column("pricingStrategy", "pricingStrategy", ColumnType.String)
            ))
            .withKeyColumns("contentTypeId");

    public static DataSource getDataSource() {
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/contentTypes.csv");

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                dataAdapter,
                                null
                        )
                )
                .withSchema(schema
                )
                .withOutput(NAME)
                .withDimensions(Arrays.asList(new Dimension("rootProductCategory", Cardinality.Int, schema.getColumn("rootProductCategory").getType())))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
