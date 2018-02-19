package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.ProductCategoryDataSource;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.report.ReportDefinition;

public class ProductCategoryReport {
        public static final String ID = "productCategory";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "productCategory")
                        .withDataSource(ProductCategoryDataSource.NAME)
                        .withParameter("parentCategoryId", "Parent Category Id", String[].class)
                        .withNodes(
                                new FilterNode("categoryFilter")
                                        .withExpression("parentCategoryId == parentCategoryId")
                                        .withConnection("#input")
                        )

                        .withOutput("categoryFilter");
        }
}
