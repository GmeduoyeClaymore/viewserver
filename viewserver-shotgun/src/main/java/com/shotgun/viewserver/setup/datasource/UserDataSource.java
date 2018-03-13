package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.adapters.common.DataLoader;
import io.viewserver.adapters.csv.CsvDataAdapter;
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
        CsvDataAdapter dataAdapter = new CsvDataAdapter();
        dataAdapter.setFileName("data/user.csv");

        return new DataSource()
                .withName(NAME)
                .withDataLoader(
                        new DataLoader(
                                NAME,
                                dataAdapter,
                                null
                        )
                )
                .withSchema(new Schema()
                                .withColumns(Arrays.asList(
                                        new Column("userId", "userId", ColumnType.String),
                                        new Column("created", "created", ColumnType.DateTime),
                                        new Column("dob", "dob", ColumnType.DateTime),
                                        new Column("lastModified", "lastModified", ColumnType.DateTime),
                                        new Column("firstName", "firstName", ColumnType.String),
                                        new Column("lastName", "lastName", ColumnType.String),
                                        new Column("password", "password", ColumnType.String),
                                        new Column("contactNo", "contactNo", ColumnType.String),
                                        new Column("selectedContentTypes", "selectedContentTypes", ColumnType.String),
                                        new Column("email", "email", ColumnType.String),
                                        new Column("type", "type", ColumnType.String),
                                        new Column("contentTypes", "contentTypes", ColumnType.String),
                                        new Column("stripeCustomerId", "stripeCustomerId", ColumnType.String),
                                        new Column("stripeAccountId", "stripeAccountId", ColumnType.String),
                                        new Column("stripeDefaultSourceId", "stripeDefaultSourceId", ColumnType.String),
                                        new Column("fcmToken", "fcmToken", ColumnType.String),
                                        new Column("chargePercentage", "chargePercentage", ColumnType.Int),
                                        new Column("latitude", "latitude", ColumnType.Double),
                                        new Column("longitude", "longitude", ColumnType.Double),
                                        new Column("range", "range", ColumnType.Int),
                                        new Column("imageUrl", "imageUrl", ColumnType.String),
                                        new Column("online", "online", ColumnType.Bool),
                                        new Column("status", "status", ColumnType.String),
                                        new Column("statusMessage", "statusMessage", ColumnType.String)
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
                .withDimensions(Arrays.asList(new Dimension("dimension_userId", Cardinality.Byte, ColumnType.String), new Dimension("status", Cardinality.Byte, ColumnType.String, true), new Dimension("online", Cardinality.Byte, ColumnType.Bool, true)))
                .withOutput("ratingJoin")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
