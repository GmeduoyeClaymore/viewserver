package com.shotgun.viewserver.setup.datasource;

import io.viewserver.datasource.*;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
//name	origin	destination	noPeople	fromTime	toTime	hasVehicle	rootProductCategory


public class
        ContentTypeDataSource {
    public static final String NAME = "contentType";

    public static final SchemaConfig schema = new SchemaConfig()
            .withColumns(Arrays.asList(
                    new Column("contentTypeId", ColumnType.Int),
                    new Column("name",ColumnType.String),
                    new Column("origin",  ColumnType.Bool),
                    new Column("destination",ColumnType.Bool),
                    new Column("noPeople", ColumnType.Bool),
                    new Column("tillTime", ColumnType.Bool),
                    new Column("fromTime",  ColumnType.Bool),
                    new Column("doubleComplete",  ColumnType.Bool),
                    new Column("noItems", ColumnType.Bool),
                    new Column("rootProductCategory",  ColumnType.String),
                    new Column("description", ColumnType.String),
                    new Column("pricingStrategy",  ColumnType.String)
            ))
            .withKeyColumns("contentTypeId");

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(schema)
                .withOutput(NAME)
                .withDimensions(Arrays.asList(new Dimension("dimension_contentTypeId","contentTypeId" ,Cardinality.Int, ColumnType.Int, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
