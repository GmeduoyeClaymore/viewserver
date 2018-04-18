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
                                        new Column("userId", ColumnType.String),
                                        new Column("created", ColumnType.DateTime),
                                        new Column("dob", ColumnType.Date),
                                        new Column("lastModified", ColumnType.DateTime),
                                        new Column("firstName", ColumnType.String),
                                        new Column("lastName", ColumnType.String),
                                        new Column("password", ColumnType.String),
                                        new Column("contactNo", ColumnType.String),
                                        new Column("selectedContentTypes", ColumnType.String),
                                        new Column("email",  ColumnType.String),
                                        new Column("type",  ColumnType.String),
                                        new Column("stripeCustomerId",  ColumnType.String),
                                        new Column("stripeAccountId", ColumnType.String),
                                        new Column("stripeDefaultSourceId",  ColumnType.String),
                                        new Column("fcmToken",  ColumnType.String),
                                        new Column("chargePercentage",  ColumnType.Int),
                                        new Column("latitude", ColumnType.Double),
                                        new Column("longitude", ColumnType.Double),
                                        new Column("range",  ColumnType.Int),
                                        new Column("imageUrl",  ColumnType.String),
                                        new Column("online",ColumnType.Bool),
                                        new Column("userStatus",  ColumnType.String),
                                        new Column("statusMessage",  ColumnType.String)
                                ))
                                .withKeyColumns("userId")
                )
                .withCalculatedColumns(
                        new CalculatedColumn("userConstantJoinCol", ColumnType.Int, "1")
                )
				.withNodes(
                        new GroupByNode("ratingGroupBy")
                                .withGroupByColumns("userId")
                                .withSummary("ratingAvg", "avg", "rating")
                                .withConnection(IDataSourceRegistry.getOperatorPath(RatingDataSource.NAME, RatingDataSource.NAME)),
                        new JoinNode("ratingJoin")
                                .withLeftJoinColumns("userId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withConnection(UserDataSource.NAME, Constants.OUT, "left")
                                .withConnection("ratingGroupBy", Constants.OUT, "right")
                )
                .withCalculatedColumns(new CalculatedColumn("dimension_userId", ColumnType.String, "userId"))
                .withDimensions(Arrays.asList(new Dimension("dimension_userId", Cardinality.Byte, ColumnType.String), new Dimension("online", Cardinality.Byte, ColumnType.Bool, true)))
                .withOutput("ratingJoin")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
