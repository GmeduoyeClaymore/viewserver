package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.OrderItemsDataSource;
import io.viewserver.Constants;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.GroupByNode;
import io.viewserver.report.ReportDefinition;

public class CartSummaryReport {

        public static final String ID = "cartSummary";

        public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, "cartSummary")
                .withDataSource(OrderItemsDataSource.NAME)
                .withParameter("userId", "User Id", String[].class)
                .withNodes(
                        new FilterNode("customerFilter")
                                .withExpression("userId == \"{userId}\" && orderId == null")
                                .withConnection("#input", null, Constants.IN),
                        new GroupByNode("totalsGroupBy")
                                .withGroupByColumns("orderId")
                                .withSummary("totalQuantity", "sum", "quantity")
                                .withSummary("totalPrice", "sum", "totalPrice")
                                .withConnection("customerFilter")
                )
                .withOutput("totalsGroupBy");
    }
}

