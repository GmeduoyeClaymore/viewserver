package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.ClusterDataSource;
import io.viewserver.report.ReportDefinition;

public class ClusterReport {
        public static final String ID = "cluster";

        public static ReportDefinition getReportDefinition() {
                return new ReportDefinition(ID, "cluster")
                        .withDataSource(ClusterDataSource.NAME)
                        .withOutput("#input");
        }
}
