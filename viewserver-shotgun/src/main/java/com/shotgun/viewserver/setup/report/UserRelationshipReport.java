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
        public static String MainuserOperatorName = IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME);

        public static List<IGraphNode> getSharedGraphNodes(String userOperatorName, boolean showUnrelated){
                return Arrays.asList(
                        new CalcColNode("userRelationships")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("isRelated", "isNull(\"{@userId}\" == fromUserId, \"{@userId}\" == toUserId) && relationshipStatus != \"UNKNOWN\""),
                                        new CalcColOperator.CalculatedColumn("initiatedByMe", "\"{@userId}\" == fromUserId"),
                                        new CalcColOperator.CalculatedColumn("relatedToUserId", "if(\"{@userId}\" == fromUserId, toUserId, fromUserId)")
                                )
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserRelationshipDataSource.NAME)),
                        new FilterNode("relatedFilter")
                                .withExpression("isRelated")
                                .withConnection("userRelationships"),
                        new GroupByNode("uniqueUserGroupBy")
                                .withGroupByColumns("relatedToUserId","relationshipStatus", "initiatedByMe")
                                .withConnection("relatedFilter"),

                        new JoinNode("relatedToUser")
                                .withLeftJoinColumns("relatedToUserId")
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("","relatedToUser_")
                                .withAlwaysResolveNames()
                                .withRightJoinOuter(showUnrelated)
                                .withConnection("uniqueUserGroupBy", Constants.OUT, "left")
                                .withConnection(userOperatorName, Constants.OUT, "right"),
                        new CalcColNode("meCalcCol")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("meUserId", "\"{@userId}\"")
                                )
                                .withConnection("relatedToUser"),
                        new JoinNode("meUser")
                                .withLeftJoinColumns("meUserId")
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("","meUser_")
                                .withAlwaysResolveNames()
                                .withConnection("meCalcCol", Constants.OUT, "left")
                                .withConnection(MainuserOperatorName, Constants.OUT, "right"),
                        new CalcColNode("distanceCalcCol")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("currentDistance", "distance(isNull({latitude},meUser_latitude), isNull({longitude},meUser_longitude) , relatedToUser_latitude, relatedToUser_longitude, \"M\")"),
                                        new CalcColOperator.CalculatedColumn("currentDistanceFilter", "if({showOutOfRange},0,distance(isNull({latitude},meUser_latitude), isNull({longitude},meUser_longitude) , relatedToUser_latitude, relatedToUser_longitude, \"M\"))"))
                                .withConnection("meUser"),
                        new FilterNode("distanceFilter")
                                .withExpression("currentDistanceFilter <= isNull({maxDistance},meUser_range)")
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
                                new IProjectionConfig.ProjectionColumn("relatedToUser_ratings", "ratings"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_ratingAvg", "ratingAvg"),
                                new IProjectionConfig.ProjectionColumn("relationshipStatus", "relationshipStatus"),
                                new IProjectionConfig.ProjectionColumn("currentDistance", "distance"),
                                new IProjectionConfig.ProjectionColumn("initiatedByMe", "initiatedByMe"),
                                new IProjectionConfig.ProjectionColumn("relatedToUser_statusMessage", "statusMessage")
                        ),
                        new FilterNode("meFilter")
                                .withExpression("userId != \"{@userId}\"")
                                .withConnection("userProjection")
                );
        }

        public static ReportDefinition getUsersForProductReportDefinition(boolean showUnrelated) {

            List<IGraphNode> nodes = new ArrayList<IGraphNode>(
                    Arrays.asList(
                            new FilterNode("productFilter")
                                    .withExpression("1 == 1")
                                    .withConnection("#input"),
                            new GroupByNode("uniqueUserByProductGroupBy")
                                    .withGroupByColumns("userId")
                                    .withConnection("productFilter"),
                            new JoinNode("userJoin")
                                    .withLeftJoinColumns("userId")
                                    .withRightJoinColumns("userId")
                                    .withConnection("uniqueUserByProductGroupBy", Constants.OUT, "left")
                                    .withConnection(MainuserOperatorName, Constants.OUT, "right")));
            nodes.addAll(getSharedGraphNodes("userJoin",showUnrelated));
            return new ReportDefinition(USER_FOR_PRODUCT_REPORT_ID + ((showUnrelated) ? "All" : ""), USER_FOR_PRODUCT_REPORT_ID + ((showUnrelated) ? "All" : ""))
                    .withDataSource(UserProductDataSource.NAME)
                    .withParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                    .withNonRequiredParameter("productId", "Product ID", String[].class,false, "")
                    .withParameter("latitude", "Latitude Override", double[].class)
                    .withParameter("longitude", "Longitude Override", double[].class)
                    .withParameter("maxDistance", "Max Distance Override", double[].class)
                    .withNodes(nodes.toArray(new IGraphNode[nodes.size()]))
                    .withOutput("meFilter");

        }
        public static ReportDefinition getReportDefinition(boolean showUnrelated) {
            List<IGraphNode> nodes = getSharedGraphNodes(MainuserOperatorName,showUnrelated);
            return new ReportDefinition(USER_RELATIONSHIPS + ((showUnrelated) ? "All" : ""), USER_RELATIONSHIPS + ((showUnrelated) ? "All" : ""))
                    .withParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                    .withParameter("latitude", "Latitude Override", double[].class)
                    .withParameter("longitude", "Longitude Override", double[].class)
                    .withParameter("maxDistance", "Max Distance Override", double[].class)
                    .withParameter("@userId", "User Id", String[].class)
                    .withNodes(nodes.toArray(new IGraphNode[nodes.size()]))
                    .withDataSource(UserProductDataSource.NAME)
                    .withOutput("meFilter");

        }
}
