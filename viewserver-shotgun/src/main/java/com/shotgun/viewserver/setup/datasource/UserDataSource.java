package com.shotgun.viewserver.setup.datasource;


import io.viewserver.Constants;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;

import java.util.Arrays;

/**
 * Created by bennett on 26/09/17.
 */
public class UserDataSource {
    public static final String NAME = "user";
    public static final Dimension dimension_userId = new Dimension("dimension_userId", "userId", Cardinality.Byte, ContentType.String, true);

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
                                        new Column("selectedContentTypes", ContentType.Json),
                                        new Column("email",  ContentType.String),
                                        new Column("bankAccount",  ContentType.Json),
                                        new Column("vehicle",  ContentType.Json),
                                        new Column("paymentCards",  ContentType.Json),
                                        new Column("relationships",  ContentType.Json),
                                        new Column("pendingMessages",  ContentType.Json),
                                        new Column("ratings",  ContentType.Json),
                                        new Column("deliveryAddress",  ContentType.Json),
                                        new Column("ratingAvg",  ContentType.Double),
                                        new Column("type",  ContentType.String),
                                        new Column("stripeCustomerId",  ContentType.String),
                                        new Column("stripeAccountId", ContentType.String),
                                        new Column("fcmToken",  ContentType.String),
                                        new Column("chargePercentage",  ContentType.Int),
                                        new Column("latitude", ContentType.Double),
                                        new Column("longitude", ContentType.Double),
                                        new Column("range",  ContentType.Int),
                                        new Column("version",  ContentType.Int),
                                        new Column("imageUrl",  ContentType.String),
                                        new Column("online", ContentType.Bool),
                                        new Column("userStatus",  ContentType.String),
                                        new Column("userAppStatus",  ContentType.String),
                                        new Column("statusMessage",  ContentType.String)
                                ))
                                .withKeyColumns("userId")
                )
                .withNodes(
                        new ProjectionNode("latLongProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Projection)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("latitude", "sourceLatitude"),
                                        new IProjectionConfig.ProjectionColumn("longitude", "sourceLongitude")
                                )
                                .withConnection(DataSource.TABLE_NAME),
                        new CalcColNode("latLongCalc")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("latitude", "getLatLongFromAddress(\"latitude\",deliveryAddress,sourceLatitude)"),
                                        new CalcColOperator.CalculatedColumn("longitude", "getLatLongFromAddress(\"longitude\",deliveryAddress,sourceLongitude)")
                                )
                                .withConnection("latLongProjection"),
                        new ProjectionNode("removeSourceCols")
                                .withMode(IProjectionConfig.ProjectionMode.Exclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("sourceLatitude"),
                                        new IProjectionConfig.ProjectionColumn( "sourceLongitude")
                                )
                                .withConnection("latLongCalc")

                )
                .withDimensions(Arrays.asList(dimension_userId,
                        new Dimension("dimension_online","online", Cardinality.Byte, ContentType.Bool)))
                .withOutput("removeSourceCols")
                .withOptions(DataSourceOption.IsReportSource, DataSourceOption.IsKeyed);
    }
}
