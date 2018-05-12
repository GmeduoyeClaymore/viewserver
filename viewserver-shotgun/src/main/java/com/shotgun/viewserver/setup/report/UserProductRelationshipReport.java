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

public class UserProductRelationshipReport{
    public static final String ID = "usersForProduct";
    public static ReportDefinition getReportDefinition() {
        QueryHolderConfig productIndexColumn = new QueryHolderConfig(UserProductDataSource.dimension_productId, false, "{productId}");
        QueryHolderConfig userRemoveMeColumn = new QueryHolderConfig(UserProductDataSource.dimension_userId, true, "{@userId}");
        return new ReportDefinition(ID, ID)
                .withDataSource(UserDataSource.NAME)
                .withNodes(
                        /* START - Getting users for products */
                        new IndexOutputNode(IDataSourceRegistry.getOperatorPath(UserProductDataSource.NAME, DataSource.INDEX_NAME))
                                .withDataSourceName(UserProductDataSource.NAME)
                                .withQueryHolders(productIndexColumn, userRemoveMeColumn),
                        new GroupByNode("uniqueUserForProductIds")
                                .withGroupByColumns("userId")
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserProductDataSource.NAME,DataSource.INDEX_NAME), IndexOutputNode.getNameForQueryHolders(Arrays.asList(productIndexColumn, userRemoveMeColumn)), Constants.IN),
                        new JoinNode("joinRelatedUsers")
                                .withLeftJoinColumns("userId")
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("","relatedUser_")
                                .withConnection("uniqueUserForProductIds", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME), Constants.OUT, "right"),
                        /* END - Getting users for products */
                        /* START - Getting related users */
                        new SpreadNode("userRelationshipsSpread")
                                .withInputColumn("relationships")
                                .withRemoveInputColumn()
                                .withSpreadFunction(UserRelationshipsSpreadFunction.NAME)
                                .withConnection("#input", Constants.OUT, Constants.IN),
                        new JoinNode("relatedUserJoin")
                                .withLeftJoinColumns(UserRelationshipsSpreadFunction.TO_USER_ID_COLUMN)
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("","toUser_")
                                .withRightJoinOuter("{showUnrelated}")
                                .withAlwaysResolveNames()
                                .withConnection("userRelationshipsSpread", Constants.OUT, "left")
                                .withConnection("joinRelatedUsers", Constants.OUT, "right"),
                        /* END - Getting related users */
                        /* START - Filtering by distance */
                        new CalcColNode("distanceCalcCol")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("currentDistance", "distance(isNull({latitude},latitude), isNull({longitude},longitude) , toUser_latitude, toUser_longitude, \"M\")"),
                                        new CalcColOperator.CalculatedColumn("currentDistanceFilter", "if({showOutOfRange},0,distance(isNull({latitude},latitude), isNull({longitude},longitude) , toUser_latitude, toUser_longitude, \"M\"))"))
                                .withConnection("relatedUserJoin"),
                        new FilterNode("distanceFilter")
                                .withExpression("currentDistanceFilter <= isNull({maxDistance},range)")
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
                                .withConnection("distanceFilter"),
                                        new ProjectionNode("userProjectionInclusionary")
                                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                                .withProjectionColumns(
                                                        new IProjectionConfig.ProjectionColumn("toUser_relatedUser_userId", "userId"),
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
                                                )
                .withConnection("userProjection")
                        /* END - Filtering by distance */

                )
                .withDefaultDimensionValues(new DefaultDimensionValues("dimension_userId").withValue(false,"@userId"))
                .withRequiredParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                .withRequiredParameter("showUnrelated", "Show Unrelated users", boolean[].class)
                .withParameter("productId", "Product ID", String[].class,false, "")
                .withRequiredParameter("latitude", "Latitude Override", double[].class)
                .withRequiredParameter("longitude", "Longitude Override", double[].class)
                .withRequiredParameter("maxDistance", "Max Distance Override", double[].class)
                .withOutput("userProjectionInclusionary");
    }
}
