package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.UserDataSource;
import com.shotgun.viewserver.setup.datasource.UserProductDataSource;
import com.shotgun.viewserver.user.UserRelationshipsSpreadFunction;
import io.viewserver.Constants;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.index.QueryHolderConfig;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.DefaultDimensionValues;
import io.viewserver.report.ReportDefinition;

import java.util.Arrays;

public class UserRelationshipReport {
    public static final String ID = "userRelationships";
    public static final QueryHolderConfig userRemoveMeColumn = new QueryHolderConfig(UserProductDataSource.dimension_userId, true, "{@userId}");

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, ID)
                .withDataSource(UserDataSource.NAME)
                .withNodes(
                        new IndexOutputNode(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, DataSource.INDEX_NAME))
                                .withDataSourceName(UserDataSource.NAME)
                                .withQueryHolders(userRemoveMeColumn),
                        new SpreadNode("userRelationshipsSpread")
                                .withInputColumn("relationships")
                                .withRemoveInputColumn()
                                .withSpreadFunction(UserRelationshipsSpreadFunction.NAME)
                                .withConnection("#input", Constants.OUT, Constants.IN),
                        new JoinNode("relatedUserJoin")
                                .withLeftJoinColumns(UserRelationshipsSpreadFunction.TO_USER_ID_COLUMN)
                                .withRightJoinColumns("userId")
                                .withRightJoinOuter("{showUnrelated}")
                                .withColumnPrefixes("", "toUser_")
                                .withAlwaysResolveNames()
                                .withConnection("userRelationshipsSpread", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, DataSource.INDEX_NAME), IndexOutputNode.getNameForQueryHolders(Arrays.asList(userRemoveMeColumn)), "right"),
                        new CalcColNode("distanceCalcCol")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("currentDistance", "distance(isNull({latitude},latitude), isNull({longitude},longitude) , toUser_latitude, toUser_longitude, \"M\")"),
                                        new CalcColOperator.CalculatedColumn("currentDistanceFilter", "if({showOutOfRange},0,distance(isNull({latitude},latitude), isNull({longitude},longitude) , toUser_latitude, toUser_longitude, \"M\"))"))
                                .withConnection("relatedUserJoin"),
                        new FilterNode("userFilter")
                                .withExpression("currentDistanceFilter <= isNull({maxDistance},range) && toUser_type == \"partner\" && !isBlocked(\"{@userId}\", toUser_relationships)")
                                .withConnection("distanceCalcCol"),
                        new ProjectionNode("userProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Exclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("password"),
                                        new IProjectionConfig.ProjectionColumn("stripeCustomerId"),
                                        new IProjectionConfig.ProjectionColumn("stripeAccountId"),
                                        new IProjectionConfig.ProjectionColumn("toUser_password"),
                                        new IProjectionConfig.ProjectionColumn("toUser_stripeCustomerId"),
                                        new IProjectionConfig.ProjectionColumn("toUser_stripeAccountId")
                                )
                                .withConnection("userFilter"),
                        new ProjectionNode("userProjectionInclusionary")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("toUser_userId", "userId"),
                                        new IProjectionConfig.ProjectionColumn("relationshipType", "relationshipType"),
                                        new IProjectionConfig.ProjectionColumn("relationshipStatus", "relationshipStatus"),
                                        new IProjectionConfig.ProjectionColumn("toUser_firstName", "firstName"),
                                        new IProjectionConfig.ProjectionColumn("toUser_lastName", "lastName"),
                                        new IProjectionConfig.ProjectionColumn("toUser_contactNo", "contactNo"),
                                        new IProjectionConfig.ProjectionColumn("toUser_selectedContentTypes", "selectedContentTypes"),
                                        new IProjectionConfig.ProjectionColumn("toUser_email", "email"),
                                        new IProjectionConfig.ProjectionColumn("toUser_type", "type"),
                                        new IProjectionConfig.ProjectionColumn("toUser_latitude", "latitude"),
                                        new IProjectionConfig.ProjectionColumn("toUser_longitude", "longitude"),
                                        new IProjectionConfig.ProjectionColumn("toUser_range", "range"),
                                        new IProjectionConfig.ProjectionColumn("toUser_imageUrl", "imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("toUser_online", "online"),
                                        new IProjectionConfig.ProjectionColumn("toUser_statusMessage", "statusMessage"),
                                        new IProjectionConfig.ProjectionColumn("toUser_ratings", "ratings"),
                                        new IProjectionConfig.ProjectionColumn("toUser_ratingAvg", "ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("toUser_rank", "rank"),
                                        new IProjectionConfig.ProjectionColumn("currentDistance", "distance")
                                ).withConnection("userProjection")

                )
                .withDefaultDimensionValues(new DefaultDimensionValues("dimension_userId").withValue(false, "@userId"))
                .withRequiredParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                .withRequiredParameter("showUnrelated", "Show Unrelated users", boolean[].class)
                .withRequiredParameter("latitude", "Latitude Override", double[].class)
                .withRequiredParameter("longitude", "Longitude Override", double[].class)
                .withRequiredParameter("maxDistance", "Max Distance Override", double[].class)
                .withRequiredParameter("@userId", "User Id", String[].class)
                .withOutput("userProjectionInclusionary");
    }
}
