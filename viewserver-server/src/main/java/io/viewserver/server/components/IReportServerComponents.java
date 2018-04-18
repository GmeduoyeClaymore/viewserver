package io.viewserver.server;

import io.viewserver.datasource.DataSourceRegistry;
import io.viewserver.execution.SystemReportExecutor;
import io.viewserver.report.ReportContextRegistry;
import io.viewserver.report.ReportRegistry;

public interface IReportServerComponents extends IServerComponent{

    ReportRegistry getReportRegistry();

    ReportContextRegistry getReportContextRegistry();

    SystemReportExecutor getSystemReportExecutor();
}
