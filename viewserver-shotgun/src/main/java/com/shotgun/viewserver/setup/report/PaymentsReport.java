package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.OrderWithPartnerDataSource;
import com.shotgun.viewserver.setup.datasource.PaymentDataSource;
import io.viewserver.execution.nodes.CalcColNode;
import io.viewserver.execution.nodes.FilterNode;
import io.viewserver.execution.nodes.ProjectionNode;
import io.viewserver.operators.calccol.CalcColOperator;
import io.viewserver.operators.projection.IProjectionConfig;
import io.viewserver.report.ReportDefinition;

public class PaymentsReport {
        public static final String ID = "paymentsReport";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "orderRequest")
                        .withDataSource(PaymentDataSource.NAME)
                        .withOutput("#input");
        }
}
