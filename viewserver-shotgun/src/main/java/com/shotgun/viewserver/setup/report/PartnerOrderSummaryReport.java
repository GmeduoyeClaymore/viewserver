package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
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
                                new CalcColNode("orderCalcs")
                                        .withCalculations(
                                                new CalcColOperator.CalculatedColumn("userCreatedThisOrder", "userId == \"{@userId}\""),
                                                new CalcColOperator.CalculatedColumn("responseStatus", "getResponseField(\"{@userId}\",\"responseStatus\",orderDetails)"),
                                                new CalcColOperator.CalculatedColumn("responsePrice", "getResponseField(\"{@userId}\",\"price\",orderDetails)"),
                                                new CalcColOperator.CalculatedColumn("responseDate", "getResponseField(\"{@userId}\",\"date\",orderDetails)")
                                        )
                                        .withConnection("customerJoin"),
                                new FilterNode("notIsBlocked")
                                        .withExpression("getRelationship(\"{@userId}\",customer_relationships) != \"BLOCKED\"")
                                        .withConnection("orderCalcs"),
                                new ProjectionNode("orderSummaryProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("responseStatus"),
                                                new IProjectionConfig.ProjectionColumn("responsePrice"),
                                                new IProjectionConfig.ProjectionColumn("responseDate"),
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
                                                new IProjectionConfig.ProjectionColumn("customer_latitude"),
                                                new IProjectionConfig.ProjectionColumn("customer_longitude"),
                                                new IProjectionConfig.ProjectionColumn("customer_firstName"),
                                                new IProjectionConfig.ProjectionColumn("customer_lastName"),
                                                new IProjectionConfig.ProjectionColumn("customer_email"),
                                                new IProjectionConfig.ProjectionColumn("customer_imageUrl"),
                                                new IProjectionConfig.ProjectionColumn("customer_online"),
                                                new IProjectionConfig.ProjectionColumn("customer_userStatus"),
                                                new IProjectionConfig.ProjectionColumn("customer_statusMessage"),
                                                new IProjectionConfig.ProjectionColumn("customer_ratingAvg"),
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
                                                        .withConnection("notIsBlocked")
                        )
                        .withOutput("orderSummaryProjection");
        }
}
