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
public class ClusterDataSource {
    public static final String NAME = "cluster";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("url", ContentType.String),
                                        new Column("clientVersion", ContentType.String),
                                        new Column("isMaster", ContentType.Bool),
                                        new Column("isOffline", ContentType.Bool),
                                        new Column("version", ContentType.Int),
                                        new Column("noConnections", ContentType.Int)
                                ))
                                .withKeyColumns("url")
                )
                .withOutput(DataSource.TABLE_NAME)
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_clientVersion","clientVersion" ,Cardinality.Int, ContentType.String),
                        new Dimension("dimension_isOffline","isOffline" ,Cardinality.Int, ContentType.Bool)))
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
