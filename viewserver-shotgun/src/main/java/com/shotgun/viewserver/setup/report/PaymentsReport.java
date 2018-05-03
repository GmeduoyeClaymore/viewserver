package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.PaymentDataSource;
import io.viewserver.report.ReportDefinition;

public class PaymentsReport {
        public static final String ID = "paymentsReport";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "orderRequest")
                        .withDataSource(PaymentDataSource.NAME)
                        .withOutput("#input");
        }
}
