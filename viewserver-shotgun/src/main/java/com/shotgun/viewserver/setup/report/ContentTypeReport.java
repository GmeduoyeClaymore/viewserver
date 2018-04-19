package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.ContentTypeDataSource;
import com.shotgun.viewserver.setup.datasource.ProductCategoryDataSource;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.JoinNode;
import io.viewserver.report.ReportDefinition;

public class ContentTypeReport {
    public static final String ID = "contentTypeCategory";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, "contentTypeCategory")
                .withDataSource(ContentTypeDataSource.NAME)
                .withNodes(
                        new JoinNode("productCategoryJoin")
                                .withLeftJoinColumns("rootProductCategory")
                                .withRightJoinColumns("categoryId")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ContentTypeDataSource.NAME), Constants.OUT, "left")
                                .withConnection(IDataSourceRegistry.getDefaultOperatorPath(ProductCategoryDataSource.NAME), Constants.OUT, "right")
                )
                .withOutput("productCategoryJoin");
    }
}
