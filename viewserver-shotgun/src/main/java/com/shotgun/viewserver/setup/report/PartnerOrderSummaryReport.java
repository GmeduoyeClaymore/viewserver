package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class PartnerOrderSummaryReport {
        public static final String ID = "partnerOrderSummary";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "partnerOrderSummary")
                        .withDataSource(OrderDataSource.NAME)
                        .withNodes(
                                new JoinNode("customerJoin")
                                        .withLeftJoinColumns("userId")
                                        .withRightJoinColumns("userId")
                                        .withConnection("#input", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getDefaultOperatorPath(UserDataSource.NAME), Constants.OUT, "right"),
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
                                                new IProjectionConfig.ProjectionColumn("requiredDate"),
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("productName"),
                                                new IProjectionConfig.ProjectionColumn("path"),
                                                new IProjectionConfig.ProjectionColumn("contentTypeRootProductCategory"),
                                                new IProjectionConfig.ProjectionColumn("status")
                                        )
                                                        .withConnection("customerJoin")
                        )
                        .withOutput("orderSummaryProjection");
        }
}
