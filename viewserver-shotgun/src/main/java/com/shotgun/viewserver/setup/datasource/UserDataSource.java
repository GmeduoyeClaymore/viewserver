package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.JoinNode;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class UserDataSource {
    public static final String NAME = "user";

    public static DataSource getDataSource() {
        return new DataSource()
                .withName(NAME)
                .withSchema(new SchemaConfig()
                                .withColumns(Arrays.asList(
                                        new Column("userId", ContentType.String),
                                        new Column("created", ContentType.DateTime),
                                        new Column("dob", ContentType.Date),
                                        new Column("lastModified", ContentType.DateTime),
                                        new Column("firstName", ContentType.String),
                                        new Column("lastName", ContentType.String),
                                        new Column("password", ContentType.String),
                                        new Column("contactNo", ContentType.String),
                                        new Column("selectedContentTypes", ContentType.String),
                                        new Column("email",  ContentType.String),
                                        new Column("type",  ContentType.String),
                                        new Column("stripeCustomerId",  ContentType.String),
                                        new Column("stripeAccountId", ContentType.String),
                                        new Column("stripeDefaultSourceId",  ContentType.String),
                                        new Column("fcmToken",  ContentType.String),
                                        new Column("chargePercentage",  ContentType.Int),
                                        new Column("latitude", ContentType.Double),
                                        new Column("longitude", ContentType.Double),
                                        new Column("range",  ContentType.Int),
                                        new Column("imageUrl",  ContentType.String),
                                        new Column("online", ContentType.Bool),
                                        new Column("userStatus",  ContentType.String),
                                        new Column("statusMessage",  ContentType.String)
                                ))
                                .withKeyColumns("userId")
                )
                .withCalculatedColumns(
                        new CalculatedColumn("userConstantJoinCol", ContentType.Int, "1")
                )
				.withNodes(
                        new GroupByNode("ratingGroupBy")
                                .withGroupByColumns("userId")
                                .withSummary("ratingAvg", "avg", "rating")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(RatingDataSource.NAME)),
                        new JoinNode("ratingJoin")
                                .withLeftJoinColumns("userId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withConnection(DataSource.TABLE_NAME, Constants.OUT, "left")
                                .withConnection("ratingGroupBy", Constants.OUT, "right")
                )
                .withDimensions(Arrays.asList(new Dimension("dimension_userId","userId", Cardinality.Byte, ContentType.String), new Dimension("online", Cardinality.Byte, ContentType.Bool)))
                .withOutput("ratingJoin")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
