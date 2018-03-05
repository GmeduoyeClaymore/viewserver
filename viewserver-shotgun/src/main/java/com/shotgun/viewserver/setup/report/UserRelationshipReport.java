package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserRelationshipReport {
        public static final String USER_FOR_PRODUCT_REPORT_ID = "usersForProduct";
        public static final String USER_RELATIONSHIPS = "userRelationships";

        public static List<IGraphNode> getSharedGraphNodes(String userOperatorName){
                return Arrays.asList(
                        new CalcColNode("userRelationships")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("isRelated", "isNull(\"{@userId}\" == fromUserId, \"{@userId}\" == toUserId)"),
                                        new CalcColOperator.CalculatedColumn("relatedToUserId", "if(\"{@userId}\" == fromUserId, toUserId, fromUserId)")
                                )
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserRelationshipDataSource.NAME, UserRelationshipDataSource.NAME)),
                        new FilterNode("relatedFilter")
                                .withExpression("isNull(isRelated,{showUnrelated})")
                                .withConnection("userRelationships"),
                        new GroupByNode("uniqueUserGroupBy")
                                .withGroupByColumns("relatedToUserId")
                                .withConnection("relatedFilter"),
                        new JoinNode("relatedToUser")
                                .withLeftJoinColumns("relatedToUserId")
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("","relatedToUser_")
                                .withAlwaysResolveNames()
                                .withConnection("uniqueUserGroupBy", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME), Constants.OUT, "right"),

                        new JoinNode("userRelationshipJoin")
                                .withLeftJoinColumns("userId")
                                .withRightJoinColumns("relatedToUser_userId")
                                .withConnection(userOperatorName, Constants.OUT, "left")
                                .withConnection("relatedToUser", Constants.OUT, "right"),

                        new CalcColNode("distanceCalcCol")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("currentDistance", "distance(isNull({latitude},latitude), isNull({longitude},longitude) , relatedToUser_latitude, relatedToUser_longitude, \"M\")"),
                                        new CalcColOperator.CalculatedColumn("currentDistanceFilter", "if({showOutOfRange},0,distance(isNull({latitude},latitude), isNull({longitude},longitude) , relatedToUser_latitude, relatedToUser_longitude, \"M\"))"))
                                .withConnection("userRelationshipJoin"),
                        new FilterNode("distanceFilter")
                                .withExpression("currentDistanceFilter <= isNull({maxDistance},range)")
                                .withConnection("distanceCalcCol"),
                        new ProjectionNode("userProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withConnection("distanceFilter")
                                .withProjectionColumns(
                                new IProjectionConfig.ProjectionColumn("relatedToUser_userId", "userId"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_firstName", "firstName"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_lastName", "lastName"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_contactNo", "contactNo"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_email", "email"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_selectedContentTypes", "selectedContentTypes"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_type", "type"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_latitude", "latitude"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_longitude", "longitude"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_range", "range"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_imageUrl", "imageUrl"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_online", "online"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_status", "status"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_status", "status"),
                                new IProjectionConfig.ProjectionColumn("currentDistance", "distance"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_statusMessage", "statusMessage")
                        )
                );
        }

        public static ReportDefinition getUsersForProductReportDefinition() {
            List<IGraphNode> nodes = new ArrayList<IGraphNode>(
                    Arrays.asList(
                            new FilterNode("productFilter")
                                    .withExpression("product_productId == \"{productId}\"")
                                    .withConnection("#input", null, Constants.IN),
                            new GroupByNode("uniqueUserGroupBy")
                                    .withGroupByColumns("userId")
                                    .withConnection("productFilter"),
                            new JoinNode("userJoin")
                                    .withLeftJoinColumns("userId")
                                    .withRightJoinColumns("userId")
                                    .withConnection("uniqueUserGroupBy", Constants.OUT, "left")
                                    .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME), Constants.OUT, "right")));
            nodes.addAll(getSharedGraphNodes("userJoin"));
            return new ReportDefinition(USER_FOR_PRODUCT_REPORT_ID, USER_FOR_PRODUCT_REPORT_ID)
                    .withDataSource(UserRelationshipDataSource.NAME)
                    .withParameter("showUnrelated", "Show Unrelated", boolean[].class)
                    .withParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                    .withParameter("productId", "Product ID", String[].class)
                    .withParameter("latitude", "Latitude Override", double[].class)
                    .withParameter("longitude", "Longitude Override", double[].class)
                    .withParameter("maxDistance", "Max Distance Override", double[].class)
                    .withNodes(nodes.toArray(new IGraphNode[nodes.size()]))
                    .withOutput("userProjection");

        }
        public static ReportDefinition getReportDefinition() {
            List<IGraphNode> nodes = getSharedGraphNodes(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME));
            return new ReportDefinition(USER_RELATIONSHIPS, USER_RELATIONSHIPS)
                    .withParameter("showUnrelated", "Show Unrelated", boolean[].class)
                    .withParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                    .withParameter("latitude", "Latitude Override", double[].class)
                    .withParameter("longitude", "Longitude Override", double[].class)
                    .withParameter("maxDistance", "Max Distance Override", double[].class)
                    .withParameter("_userId", "User Id", String[].class)
                    .withNodes(nodes.toArray(new IGraphNode[nodes.size()]))
                    .withDataSource(UserRelationshipDataSource.NAME)
                    .withOutput("userProjection");

        }
}
