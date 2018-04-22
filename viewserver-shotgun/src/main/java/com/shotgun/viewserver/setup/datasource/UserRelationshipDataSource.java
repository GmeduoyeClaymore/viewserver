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
                                new Column("relationshipId", ContentType.String),
                                new Column("fromUserId",  ContentType.String),
                                new Column("toUserId",  ContentType.String),
                                new Column("relationshipStatus",  ContentType.String),
                                new Column("relationshipType",  ContentType.String)
                        ))
                        .withKeyColumns("relationshipId")
                )
                .withDimensions(Arrays.asList(
                        new Dimension("dimension_relationshipType","relationshipType", Cardinality.Int, ContentType.String)))
                .withOutput(DataSource.TABLE_NAME)
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
