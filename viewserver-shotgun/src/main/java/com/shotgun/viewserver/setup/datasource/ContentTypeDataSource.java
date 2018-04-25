package com.shotgun.viewserver.setup.datasource;

import com.shotgun.viewserver.IShotgunViewServerConfiguration;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.datasource.*;

import java.util.Arrays;

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
                    new Column("hasOrigin", "hasOrigin", ColumnType.Bool),
                    new Column("hasDestination", "hasDestination", ColumnType.Bool),
                    new Column("hasStartTime", "hasStartTime", ColumnType.Bool),
                    new Column("hasEndTime", "hasEndTime", ColumnType.Bool),
                    new Column("rootProductCategory", "rootProductCategory", ColumnType.String), new Column("description", "description", ColumnType.String),
                    new Column("pricingStrategy", "pricingStrategy", ColumnType.String)
                    ))
            .withKeyColumns("contentTypeId");

    public static DataSource getDataSource(IShotgunViewServerConfiguration shotgunConfiguration) {
        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                DataSourceUtils.getCsvDataAdapter(shotgunConfiguration, NAME, "data/contentTypes.csv", true),
                                null
                        )
                )
                .withSchema(schema)
                .withCalculatedColumns(new CalculatedColumn("dimension_contentTypeId", ColumnType.Int, "contentTypeId"))
                .withOutput(NAME)
                .withDimensions(Arrays.asList(new Dimension("dimension_contentTypeId", Cardinality.Int, ColumnType.Int, true)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
