package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import com.shotgun.viewserver.setup.datasource.OrderItemsDataSource;
import com.shotgun.viewserver.setup.datasource.ProductDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class OrderSummaryReport {
        public static final String ID = "orderSummary";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "orderSummary")
                        .withDataSource(OrderItemsDataSource.NAME)
                        .withParameter("customerId", "Customer Id", String[].class)
                        .withParameter("isCompleted", "Is Order Complete", Boolean[].class)
                        .withParameter("orderId", "Order Id", String[].class)
                        .withNodes(
                                new FilterNode("orderFilter")
                                        .withExpression("customerId == \"{customerId}\" && if(\"{orderId}\" != \"\", orderId == \"{orderId}\", orderId != null)")
                                        .withConnection("#input", null, Constants.IN),
                                new GroupByNode("totalsGroupBy")
                                        .withGroupByColumns("orderId")
                                        .withSummary("totalQuantity", "sum", "quantity")
                                        .withSummary("totalPrice", "sum", "totalPrice")
                                        .withConnection("orderFilter"),
                                new JoinNode("orderSummaryJoin")
                                        .withLeftJoinColumns("orderId")
                                        .withRightJoinColumns("orderId")
                                        .withConnection("totalsGroupBy", Constants.OUT, "left")
                                        .withConnection(IDataSourceRegistry.getOperatorPath(OrderDataSource.NAME, OrderDataSource.NAME), Constants.OUT, "right"),
                                new FilterNode("statusFilter")
                                        .withExpression("if({isCompleted} == true, status == \"COMPLETED\", status != \"COMPLETED\")")
                                        .withConnection("orderSummaryJoin"),
                                new ProjectionNode("orderSummaryProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Inclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("orderId"),
                                                new IProjectionConfig.ProjectionColumn("paymentId"),
                                                new IProjectionConfig.ProjectionColumn("deliveryId"),
                                                new IProjectionConfig.ProjectionColumn("totalQuantity"),
                                                new IProjectionConfig.ProjectionColumn("totalPrice"),
                                                new IProjectionConfig.ProjectionColumn("status"),
                                                new IProjectionConfig.ProjectionColumn("created"))
                                        .withConnection("statusFilter")
                        )
                        .withOutput("orderSummaryProjection");
        }
}
