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
                    new Column("contentTypeId", ContentType.Int),
                    new Column("name", ContentType.String),
                    new Column("origin",  ContentType.Bool),
                    new Column("destination", ContentType.Bool),
                    new Column("noPeople", ContentType.Bool),
                    new Column("tillTime", ContentType.Bool),
                    new Column("fromTime",  ContentType.Bool),
                    new Column("doubleComplete",  ContentType.Bool),
                    new Column("noItems", ContentType.Bool),
                    new Column("rootProductCategory",  ContentType.String),
                    new Column("description", ContentType.String),
                    new Column("pricingStrategy",  ContentType.String)
            ))
            .withKeyColumns("contentTypeId");

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(schema)
                .withOutput(NAME)
                .withDimensions(Arrays.asList(new Dimension("dimension_contentTypeId","contentTypeId" ,Cardinality.Int, ContentType.Int, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
