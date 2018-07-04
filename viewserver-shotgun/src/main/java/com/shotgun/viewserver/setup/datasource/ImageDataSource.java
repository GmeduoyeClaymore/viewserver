package com.shotgun.viewserver.setup.datasource;


import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class ImageDataSource {
    public static final String NAME = "images";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("imageId", ContentType.String),
                                        new Column("imageData", ContentType.String),
                                        new Column("version", ContentType.Int),
                                        new Column("created", ContentType.DateTime)
                                ))
                                .withKeyColumns("imageId")
                )
                .withOutput(DataSource.TABLE_NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
