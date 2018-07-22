package io.viewserver.server.components;

import io.viewserver.command.ICommandHandler;
import io.viewserver.command.SubscribeDimensionHandler;
import io.viewserver.command.SubscribeReportHandler;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.execution.SystemReportExecutor;
import io.viewserver.report.ReportContextRegistry;
import io.viewserver.report.ReportRegistry;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;
import rx.Observable;

public class ReportServerComponents implements IReportServerComponents {
    private final IBasicServerComponents basicServerComponents;
    private IDataSourceServerComponents dataSourceServerComponents;
    private ReportRegistry reportRegistry;
    private ReportContextRegistry reportContextRegistry;
    private SystemReportExecutor systemReportExecutor;

    public ReportServerComponents(IBasicServerComponents basicServerComponents, IDataSourceServerComponents dataSourceServerComponents) {
        this.basicServerComponents = basicServerComponents;
        this.dataSourceServerComponents = dataSourceServerComponents;
    }

    @Override
    public Observable start() {

        reportRegistry = new ReportRegistry(basicServerComponents.getServerCatalog(), basicServerComponents.getExecutionContext());
        reportContextRegistry = new ReportContextRegistry(basicServerComponents.getExecutionContext(),basicServerComponents.getServerCatalog(), new ChunkedColumnStorage(1024));
        systemReportExecutor = new SystemReportExecutor(basicServerComponents.getExecutionContext().getDimensionMapper(), basicServerComponents.getExecutionPlanRunner(), getDataSourceRegistry(), reportRegistry);

        register("subscribeReport", new SubscribeReportHandler(basicServerComponents.getExecutionContext().getDimensionMapper(), getDataSourceRegistry(), reportRegistry, basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(), basicServerComponents.getExecutionPlanRunner(), reportContextRegistry, systemReportExecutor));
        register("subscribeDimension", new SubscribeDimensionHandler(basicServerComponents.getExecutionContext().getDimensionMapper(), getDataSourceRegistry(), reportRegistry, basicServerComponents.getSubscriptionManager(), basicServerComponents.getConfigurator(), basicServerComponents.getExecutionPlanRunner(), reportContextRegistry));
        return Observable.just(true);
    }

    private IDataSourceRegistry getDataSourceRegistry(){
        return this.dataSourceServerComponents.getDataSourceRegistry();
    }

    @Override
    public ReportRegistry getReportRegistry() {
        return reportRegistry;
    }

    @Override
    public ReportContextRegistry getReportContextRegistry() {
        return reportContextRegistry;
    }

    @Override
    public SystemReportExecutor getSystemReportExecutor() {
        return systemReportExecutor;
    }

    void register(String name, ICommandHandler commandHandler){
        basicServerComponents.getCommandHandlerRegistry().register(name,commandHandler);
    }

}
