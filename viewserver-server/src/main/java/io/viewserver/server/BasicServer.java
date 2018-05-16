package io.viewserver.server;

import io.viewserver.network.IEndpoint;
import io.viewserver.server.components.*;

import java.util.ArrayList;
import java.util.List;



public class BasicServer {
    private IBasicSubscriptionComponent basicSubscriptionComponent;
    private IBasicServerComponents basicServerComponents;
    private IControllerComponents controllerComponents;
    private IDataSourceServerComponents dataSourceServerComponents;
    private IReportServerComponents reportServerComponents;
    private IInitialDataLoaderComponent initialDataLoaderComponent;
    private List<Callable<IServerComponent>> components = new ArrayList<>();

    public interface Callable<V> {
        V call() ;
    }

    public BasicServer(IBasicSubscriptionComponent basicSubscriptionComponent,IBasicServerComponents basicServerComponents, IControllerComponents controllerComponents, IDataSourceServerComponents dataSourceServerComponents, IReportServerComponents reportServerComponents, IInitialDataLoaderComponent initialDataLoaderComponent) {
        this.basicSubscriptionComponent = basicSubscriptionComponent;
        this.basicServerComponents = basicServerComponents;
        this.controllerComponents = controllerComponents;
        this.dataSourceServerComponents = dataSourceServerComponents;
        this.reportServerComponents = reportServerComponents;
        this.initialDataLoaderComponent = initialDataLoaderComponent;
        registerComponent(this.basicServerComponents);
        registerComponent(this.dataSourceServerComponents);
        registerComponent(this.reportServerComponents);
        registerComponent(this.controllerComponents);
        registerComponent(this.basicSubscriptionComponent);
        registerComponent(this.initialDataLoaderComponent);
    }

    BasicServer(List<IEndpoint> endpointList) {
        basicServerComponents = new NettyBasicServerComponent(endpointList);
        dataSourceServerComponents = new DataSourceComponents(basicServerComponents);
        controllerComponents = new ControllerComponents(basicServerComponents);
        reportServerComponents = new ReportServerComponents(basicServerComponents,dataSourceServerComponents);
        this.basicSubscriptionComponent = new BasicSubscriptionComponent(basicServerComponents);
        registerComponent(basicServerComponents);
        registerComponent(this.basicSubscriptionComponent);
        registerComponent(dataSourceServerComponents);
        registerComponent(reportServerComponents);
        registerComponent(controllerComponents);
    }

    public void registerComponent(IServerComponent component) {
        registerComponent(() -> component);
    }
    public void registerComponent(Callable<IServerComponent> component) {
        this.components.add(component);
    }

    public void start(){
        this.components.forEach(c-> c.call().start());
        basicServerComponents.listen();
    }

    public void stop(){
        this.components.forEach(c-> c.call().stop());
    }
}
