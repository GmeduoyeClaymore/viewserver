package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class CustomerOrderSummaryReport {
    public static final String ID = "customerOrderSummary";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, "customerOrderSummary")
                .withDataSource(OrderDataSource.NAME)
                .withNodes(
                        new JoinNode("partnerJoin")
                                .withLeftJoinColumns("assignedPartnerUserId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("userId")
                                .withColumnPrefixes("", "partner_")
                                .withAlwaysResolveNames()
                                .withConnection("#input", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getOperatorPath(UserDataSource.NAME, "ratingJoin"), Constants.OUT, "right"),
                        new JoinNode("ratingJoin")
                                .withLeftJoinColumns("orderId", "assignedPartnerUserId")
                                .withLeftJoinOuter()
                                .withRightJoinColumns("orderId", "userId")
                                .withConnection("partnerJoin", Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(RatingDataSource.NAME), Constants.OUT, "right"),
                        new ProjectionNode("orderSummaryProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("partner_latitude"),
                                        new IProjectionConfig.ProjectionColumn("partner_longitude"),
                                        new IProjectionConfig.ProjectionColumn("partner_firstName"),
                                        new IProjectionConfig.ProjectionColumn("partner_lastName"),
                                        new IProjectionConfig.ProjectionColumn("partner_email"),
                                        new IProjectionConfig.ProjectionColumn("partner_imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("partner_online"),
                                        new IProjectionConfig.ProjectionColumn("partner_userStatus"),
                                        new IProjectionConfig.ProjectionColumn("partner_statusMessage"),
                                        new IProjectionConfig.ProjectionColumn("partner_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("orderLocation"),
                                        new IProjectionConfig.ProjectionColumn("totalPrice"),
                                        new IProjectionConfig.ProjectionColumn("orderContentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("orderDetails"),
                                        new IProjectionConfig.ProjectionColumn("orderId"),
                                        new IProjectionConfig.ProjectionColumn("status")
                                ).withConnection("ratingJoin")


                )
                .withOutput("orderSummaryProjection");
    }
}
