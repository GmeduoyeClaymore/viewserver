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

public class LegacyUserRelationshipReport {
        public static final String USER_FOR_PRODUCT_REPORT_ID = "usersForProduct";
        public static final String USER_RELATIONSHIPS = "userRelationships";
        public static String MainuserOperatorName = IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME);

        public static List<IGraphNode> getSharedGraphNodes(String userOperatorName, boolean showUnrelated){
                return Arrays.asList(
                        new CalcColNode("meCalcCol")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("meUserId", "\"{@userId}\"")
                                )
                                .withConnection("relatedToUser"),


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
                                .withExpression("userId != \"{@userId}\" && type == \"partner\"")
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
                    .withRequiredParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                    .withNonRequiredParameter("productId", "Product ID", String[].class,false, "")
                    .withRequiredParameter("latitude", "Latitude Override", double[].class)
                    .withRequiredParameter("longitude", "Longitude Override", double[].class)
                    .withRequiredParameter("maxDistance", "Max Distance Override", double[].class)
                    .withNodes(nodes.toArray(new IGraphNode[nodes.size()]))

                    .withOutput("meFilter");

        }
        public static ReportDefinition getReportDefinition(boolean showUnrelated) {
            List<IGraphNode> nodes = getSharedGraphNodes(MainuserOperatorName,showUnrelated);
            return new ReportDefinition(USER_RELATIONSHIPS + ((showUnrelated) ? "All" : ""), USER_RELATIONSHIPS + ((showUnrelated) ? "All" : ""))
                    .withRequiredParameter("showOutOfRange", "Show Out Of Range", boolean[].class)
                    .withRequiredParameter("latitude", "Latitude Override", double[].class)
                    .withRequiredParameter("longitude", "Longitude Override", double[].class)
                    .withRequiredParameter("maxDistance", "Max Distance Override", double[].class)
                    .withRequiredParameter("@userId", "User Id", String[].class)
                    .withNodes(nodes.toArray(new IGraphNode[nodes.size()]))
                    .withDataSource(UserProductDataSource.NAME)
                    .withOutput("meFilter");

        }
}


