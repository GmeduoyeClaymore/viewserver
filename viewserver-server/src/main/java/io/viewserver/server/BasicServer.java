package io.viewserver.server;

import io.viewserver.core.ExecutionContext;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.components.*;

import java.util.List;

public class BasicServer {
    private IBasicSubscriptionComponent basicSubscriptionComponent;
    private IBasicServerComponents basicServerComponents;
    private IControllerComponents controllerComponents;
    private IDataSourceServerComponents dataSourceServerComponents;
    private IReportServerComponents reportServerComponents;
    private IInitialDataLoaderComponent initialDataLoaderComponent;

    public BasicServer(IBasicSubscriptionComponent basicSubscriptionComponent,IBasicServerComponents basicServerComponents, IControllerComponents controllerComponents, IDataSourceServerComponents dataSourceServerComponents, IReportServerComponents reportServerComponents, IInitialDataLoaderComponent initialDataLoaderComponent) {
        this.basicSubscriptionComponent = basicSubscriptionComponent;
        this.basicServerComponents = basicServerComponents;
        this.controllerComponents = controllerComponents;
        this.dataSourceServerComponents = dataSourceServerComponents;
        this.reportServerComponents = reportServerComponents;
        this.initialDataLoaderComponent = initialDataLoaderComponent;
    }

    BasicServer(List<IEndpoint> endpointList) {
        basicServerComponents = new NettyBasicServerComponent(endpointList);
        dataSourceServerComponents = new DataSourceComponents(basicServerComponents);
        controllerComponents = new ControllerComponents(basicServerComponents);
        reportServerComponents = new ReportServerComponents(basicServerComponents,dataSourceServerComponents);
        this.basicSubscriptionComponent = new BasicSubscriptionComponent(basicServerComponents);
    }

    public void start(){
        this.basicServerComponents.start();
        this.basicSubscriptionComponent.start();
        this.dataSourceServerComponents.start();
        this.reportServerComponents.start();
        this.initialDataLoaderComponent.start();
        this.controllerComponents.start();
    }

    public void stop(){
        this.controllerComponents.stop();
        this.initialDataLoaderComponent.stop();
        this.reportServerComponents.stop();
        this.dataSourceServerComponents.stop();
        this.basicSubscriptionComponent.stop();
        this.basicServerComponents.stop();
    }
}
