package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.ContentTypeDataSource;
import com.shotgun.viewserver.setup.datasource.ProductCategoryDataSource;
import com.shotgun.viewserver.setup.datasource.ProductDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.ReportContext;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.report.DefaultDimensionValues;
import io.viewserver.report.ReportDefinition;

public class ProductReport {
    public static final String ID = "productReport";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, "productReport")
                .withDataSource(ProductDataSource.NAME)
                .withDefaultDimensionValues(new DefaultDimensionValues("dimension_categoryId").withDimValue("{categoryId}"))
                .withParameter("categoryId", "CategoryId", String.class, true)
                .withOutput("#input");
    }
}
