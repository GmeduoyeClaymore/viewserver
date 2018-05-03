package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import com.shotgun.viewserver.user.DateNegotiatedOrderResponseSpreadFunction;
import io.viewserver.Constants;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class CustomerOrderSummaryReport {
    public static final String ID = "customerOrderSummary";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, "customerOrderSummary")
                .withDataSource(OrderWithResponseDataSource.NAME)
                .withNodes(
                        new GroupByNode("groupPartnerDetails")
                                .withGroupByColumns("orderId")
                                .withSummary("partnerResponses","json",DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN, new Object[]{
                                        "partner_latitude",
                                        "partner_longitude",
                                        "partner_firstName",
                                        "partner_lastName",
                                        "partner_email",
                                        "partner_imageUrl",
                                        "partner_online",
                                        "partner_userStatus",
                                        "partner_statusMessage",
                                        "partner_ratingAvg",
                                        DateNegotiatedOrderResponseSpreadFunction.PARTNER_ID_COLUMN,
                                        DateNegotiatedOrderResponseSpreadFunction.ESTIMATED_DATE_COLUMN,
                                        DateNegotiatedOrderResponseSpreadFunction.PRICE_COLUMN,
                                        DateNegotiatedOrderResponseSpreadFunction.PARTNER_ORDER_STATUS,
                                })
                                .withConnection("#input"),
                        new JoinNode("orderJoin")
                                .withLeftJoinColumns("orderId")
                                .withRightJoinColumns("orderId")
                                .withLeftJoinOuter()
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(OrderWithPartnerDataSource.NAME), Constants.OUT, "right")
                                .withConnection("groupPartnerDetails", Constants.OUT, "left")
                                .withColumnPrefixes("groupPartnerDetails_","order_")
                                .withAlwaysResolveNames(),
                        new CalcColNode("orderFieldsCalc")
                                .withCalculations(
                                        new CalcColOperator.CalculatedColumn("internalOrderStatus", "getOrderField(\"status\", order_orderDetails)"))
                                .withConnection("orderJoin"),
                        new ProjectionNode("orderSummaryProjection")
                                .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                .withProjectionColumns(
                                        new IProjectionConfig.ProjectionColumn("order_partner_latitude","partner_latitude"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_longitude","partner_longitude"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_firstName","partner_firstName"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_lastName","partner_lastName"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_email","partner_email"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_imageUrl","partner_imageUrl"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_online","partner_online"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_userStatus","partner_userStatus"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_statusMessage", "partner_statusMessage"),
                                        new IProjectionConfig.ProjectionColumn("order_partner_ratingAvg","partner_ratingAvg"),
                                        new IProjectionConfig.ProjectionColumn("order_requiredDate","requiredDate"),
                                        new IProjectionConfig.ProjectionColumn("order_orderLocation","orderLocation"),
                                        new IProjectionConfig.ProjectionColumn("groupPartnerDetails_partnerResponses","partnerResponses"),
                                        new IProjectionConfig.ProjectionColumn("internalOrderStatus"),
                                        new IProjectionConfig.ProjectionColumn("order_totalPrice", "totalPrice"),
                                        new IProjectionConfig.ProjectionColumn("order_orderContentTypeId","orderContentTypeId"),
                                        new IProjectionConfig.ProjectionColumn("order_orderDetails","orderDetails"),
                                        new IProjectionConfig.ProjectionColumn("order_orderId","orderId"),
                                        new IProjectionConfig.ProjectionColumn("order_status","status")
                                ).withConnection("orderFieldsCalc")


                )
                .withOutput("orderSummaryProjection");
    }
}
