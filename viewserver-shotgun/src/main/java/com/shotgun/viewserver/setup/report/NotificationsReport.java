package com.shotgun.viewserver.setup.report;

import com.shotgun.viewserver.setup.datasource.MessagesDataSource;
import io.viewserver.report.DefaultDimensionValues;
import io.viewserver.report.ReportDefinition;

public class NotificationsReport {
    public static final String ID = "notificationsReport";

    public static ReportDefinition getReportDefinition() {
        return new ReportDefinition(ID, "notificationsReport")
                .withDataSource(MessagesDataSource.NAME)
                .withDefaultDimensionValues(new DefaultDimensionValues("dimension_toUserId").withValue(false,"@userId"))
                .withDefaultDimensionValues(new DefaultDimensionValues("dimension_fromUserId").withValue(true,"@userId"))
                .withOutput("#input");
    }
}
