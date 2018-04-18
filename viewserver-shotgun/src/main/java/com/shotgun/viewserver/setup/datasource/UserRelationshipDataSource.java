package com.shotgun.viewserver.setup.datasource;

import io.viewserver.datasource.*;

import java.util.Arrays;

public class UserRelationshipDataSource {
    public static final String NAME = "userRelationship";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                        .withColumns(Arrays.asList(
                                new Column("relationshipId", ColumnType.String),
                                new Column("fromUserId",  ColumnType.String),
                                new Column("toUserId",  ColumnType.String),
                                new Column("relationshipStatus",  ColumnType.String),
                                new Column("relationshipType",  ColumnType.String)
                        ))
                        .withKeyColumns("relationshipId")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("relationshipType", Cardinality.Int, ColumnType.String)))
                .withOutput(NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
