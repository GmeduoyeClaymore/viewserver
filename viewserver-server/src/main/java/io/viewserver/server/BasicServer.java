package io.viewserver.server;

import io.viewserver.network.IEndpoint;
import io.viewserver.server.components.*;

import java.util.List;

public class BasicServer {
    private IBasicServerComponents basicServerComponents;
    private IControllerComponents controllerComponents;
    private IDataSourceServerComponents dataSourceServerComponents;
    private IReportServerComponents reportServerComponents;
    private IInitialDataLoaderComponent initialDataLoaderComponent;

    public BasicServer(IBasicServerComponents basicServerComponents, IControllerComponents controllerComponents, IDataSourceServerComponents dataSourceServerComponents, IReportServerComponents reportServerComponents, IInitialDataLoaderComponent initialDataLoaderComponent) {
        this.basicServerComponents = basicServerComponents;
        this.controllerComponents = controllerComponents;
        this.dataSourceServerComponents = dataSourceServerComponents;
        this.reportServerComponents = reportServerComponents;
        this.initialDataLoaderComponent = initialDataLoaderComponent;
    }

    public BasicServer(List<IEndpoint> endpointList) {
        basicServerComponents = new NettyBasicServerComponent(endpointList);
        dataSourceServerComponents = new DataSourceComponents(basicServerComponents);
        controllerComponents = new ControllerComponents(basicServerComponents);
        reportServerComponents = new ReportServerComponents(basicServerComponents,dataSourceServerComponents);
    }

    public void start(){
        this.basicServerComponents.start();
        this.dataSourceServerComponents.start();
        this.reportServerComponents.start();
        this.initialDataLoaderComponent.start();
        this.controllerComponents.start();
    }
}
