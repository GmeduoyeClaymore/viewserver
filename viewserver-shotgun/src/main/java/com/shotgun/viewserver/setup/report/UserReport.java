package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class UserReport {
        public static final String ID = "userReport";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, ID)
                        .withDataSource(UserDataSource.NAME)
                        .withNodes(
                                new ProjectionNode("userProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Exclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("password"),
                                                new IProjectionConfig.ProjectionColumn("stripeCustomerId"),
                                                new IProjectionConfig.ProjectionColumn("stripeAccountId")
                                        )
                                        .withConnection("#input")
                        )
                        .withOutput("userProjection");
        }
}