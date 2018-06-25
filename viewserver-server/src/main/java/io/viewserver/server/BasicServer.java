package io.viewserver.server;

import io.viewserver.network.IEndpoint;
import io.viewserver.server.components.*;
import io.viewserver.util.dynamic.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class BasicServer {
    private IBasicSubscriptionComponent basicSubscriptionComponent;
    private IBasicServerComponents basicServerComponents;
    private IControllerComponents controllerComponents;
    private IDataSourceServerComponents dataSourceServerComponents;
    private IReportServerComponents reportServerComponents;
    private IInitialDataLoaderComponent initialDataLoaderComponent;
    private List<Callable<IServerComponent>> componentFactories = new ArrayList<>();
    private List<IServerComponent> components = new ArrayList<>();
    private Logger logger;
    private String serverName = "basicServer";
    Executor backgroundExecutor;


    public interface Callable<V> {
        V call() ;
    }

    public BasicServer(IBasicSubscriptionComponent basicSubscriptionComponent,IBasicServerComponents basicServerComponents, IControllerComponents controllerComponents, IDataSourceServerComponents dataSourceServerComponents, IReportServerComponents reportServerComponents, IInitialDataLoaderComponent initialDataLoaderComponent) {
        this.backgroundExecutor = Executors.newFixedThreadPool(1,new NamedThreadFactory("basicServer"));
        this.logger = LoggerFactory.getLogger(String.format("%s-%s",BasicServer.class,serverName));

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

    BasicServer(String serverName,List<IEndpoint> endpointList) {
        this.serverName = serverName;
        this.backgroundExecutor = Executors.newFixedThreadPool(1,new NamedThreadFactory("basicServer"));
        this.logger = LoggerFactory.getLogger(String.format("%s-%s",BasicServer.class,serverName));
        basicServerComponents = new NettyBasicServerComponent(serverName,endpointList);
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

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
        this.backgroundExecutor = Executors.newFixedThreadPool(1,new NamedThreadFactory(serverName +"basicServer"));
        this.logger = LoggerFactory.getLogger(String.format("%s-%s",BasicServer.class,serverName));
    }

    public void registerComponent(IServerComponent component) {
        registerComponent(() -> component);
    }
    public void registerComponent(Callable<IServerComponent> component) {
        this.componentFactories.add(component);
    }

    public Observable start(){
        return Observable.create(
                emitter -> {
                    List<Observable<Object>> observables = new ArrayList<>();
                    this.componentFactories.forEach(c-> {
                        IServerComponent component = c.call();
                        components.add(component);
                        Observable start = component.start();
                        if(start != null) {
                            observables.add(start);
                        }
                    });
                    FuncN<?> onCompletedAll = (FuncN<Object>) objects -> {
                        logger.info(String.format("COMPLETED FINISHED WAITING for server components"));
                        return true;
                    };
                    Observable.zip(observables, onCompletedAll).observeOn(Schedulers.from(backgroundExecutor)).take(1).timeout(20, TimeUnit.SECONDS,Observable.error(new RuntimeException("Server not started after 20 seconds. Something's gone wrong !! Could be connection to the database ??"))).subscribe(
                            res -> {
                                basicServerComponents.listen();
                                emitter.onNext(true);
                                emitter.onCompleted();
                            },
                            err -> {
                                emitter.onError(err);
                            }
                    );
                },
                Emitter.BackpressureMode.BUFFER
        );

    }

    public void stop(){
        this.components.forEach(c-> c.stop());
    }
}
