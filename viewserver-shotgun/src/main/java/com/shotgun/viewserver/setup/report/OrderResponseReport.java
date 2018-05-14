package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.OrderWithResponseDataSource;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class OrderResponseReport {
        public static final String ID = "orderResponses";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "orderResponses")
                        .withDataSource(OrderWithResponseDataSource.NAME)
                        .withNodes(
                                new CalcColNode("userCreatedThisOrderCalc")
                                        .withCalculations(new CalcColOperator.CalculatedColumn("userCreatedThisOrder", "userId == \"{@userId}\""))
                                        .withConnection("#input"),
                                new FilterNode("notIsBlocked")
                                        .withExpression("!isBlocked(\"{@userId}\",customer_relationships)")
                                        .withConnection("userCreatedThisOrderCalc"),
                                new ProjectionNode("orderRequestProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("customer_firstName"),
                                                new IProjectionConfig.ProjectionColumn("customer_lastName"),
                                                new IProjectionConfig.ProjectionColumn("customer_ratingAvg"),
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
                                                new IProjectionConfig.ProjectionColumn("requiredDate"),
                                                new IProjectionConfig.ProjectionColumn("totalPrice"),
                                                new IProjectionConfig.ProjectionColumn("orderContentTypeId"),
                                                new IProjectionConfig.ProjectionColumn("orderDetails"),
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("responseStatus"),
                                                new IProjectionConfig.ProjectionColumn("userCreatedThisOrder")
                                        )
                                        .withConnection("notIsBlocked")
                        )
                        .withOutput("orderRequestProjection");
        }
}
