package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.*;
import com.shotgun.viewserver.user.UserRelationshipsSpreadFunction;
import io.viewserver.Constants;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.nodes.*;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.index.QueryHolderConfig;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class UserReport {
        public static final String ID = "userReport";
        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, ID)
                        .withDataSource(UserDataSource.NAME)
                        .withNodes(
                                new CalcColNode("relationshipCalc")
                                        .withCalculations(
                                                new CalcColOperator.CalculatedColumn("relationshipStatus", "getRelationship(\"{@userId}\",relationships)"))
                                        .withConnection("#input"),
                                new ProjectionNode("userProjection")
                                        .withMode(IProjectionConfig.ProjectionMode.Exclusionary)
                                        .withProjectionColumns(
                                                new IProjectionConfig.ProjectionColumn("password"),
                                                new IProjectionConfig.ProjectionColumn("stripeCustomerId"),
                                                new IProjectionConfig.ProjectionColumn("stripeAccountId")
                                        )
                                        .withConnection("relationshipCalc")
                        )
                        .withRequiredParameter("@userId", "User Id", String[].class)
                        .withOutput("userProjection");
        }
}