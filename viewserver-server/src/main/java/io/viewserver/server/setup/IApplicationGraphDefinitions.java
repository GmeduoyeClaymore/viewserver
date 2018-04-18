package com.shotgun.viewserver.setup;

import io.viewserver.datasource.DataSource;
import io.viewserver.report.ReportDefinition;

import java.util.List;
import java.util.Map;

public interface IApplicationGraphDefinitions {
    List<DataSource> getDataSources();
    Map<String, ReportDefinition> getReportDefinitions();
}
