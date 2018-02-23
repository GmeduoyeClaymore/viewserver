package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class UserProductReport {
        public static final String ID = "usersForProduct";


        public static IGraphNode[] getSharedGraphNodes(){
                return new IGraphNode()
                        new CalcColNode("userRelationships")
                        .withCalculations(
                                new CalcColOperator.CalculatedColumn("isRelated", "@userId == fromUserId || @userID == toUserId"),
                                new CalcColOperator.CalculatedColumn("relatedToUserId", "if(@userId == fromUserId, toUserId, fromUserId)")
                        )
                        .withConnection(IDataSourceRegistry.getOperatorPath(UserRelationshipDataSource.NAME, UserRelationshipDataSource.NAME)),
                        new FilterNode("relatedFilter")
                                .withExpression("isRelated")
                                .withConnection("userRelationships"),
                        new JoinNode("relatedToUser")
                                .withLeftJoinColumns("relatedToUserId")
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("relatedToUser_")
                                .withAlwaysResolveNames()
                                .withConnection("relatedFilter", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME), Constants.OUT, "right"),
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
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, UserDataSource.NAME), Constants.OUT, "right"),
                        new JoinNode("userRelationshipJoin")
                                .withLeftJoinColumns("userId")
                                .withRightJoinColumns("relatedToUser_userId")
                                .withConnection("uniqueUserGroupBy", Constants.OUT, "left")
                                .withConnection("relatedToUser", Constants.OUT, "right"),

                        new CalcColNode("distanceCalcCol")
                                .withCalculations(new CalcColOperator.CalculatedColumn("currentDistance", "distance(latitude, longitude, relatedToUser_latitude, relatedToUser_longtitude, \"M\")"))
                                .withConnection("userRelationshipJoin"),
                        new FilterNode("distanceFilter")
                                .withExpression("currentDistance <= range")
                                .withConnection("distanceCalcCol"),
                        new ProjectionNode("userProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_userId","userId"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_firstName","firstName"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_lastName","lastName"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_contactNo","contactNo"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_email","email"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_selectedContentTypes","selectedContentTypes"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_type","type"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_latitude","latitude"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_longitude","longitude"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_range","range"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_imageUrl","imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_online","online"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_status","status"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_status","status"),
                                        new IProjectionConfig.ProjectionColumn("relatedToUser_statusMessage","statusMessage")

                                )
                        ]
        }

        public static ReportDefinition getReportDefinition() {

        }
        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "usersForProduct")
                        .withDataSource(UserProductDataSource.NAME)
                        .withParameter("productId", "Product ID", String[].class) // not used ?
                        .withNodes(

                                        .withConnection("distanceFilter"))
                        .withOutput("userProjection");
        }
}
