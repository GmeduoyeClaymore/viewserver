package io.viewserver.server.components;

import io.viewserver.command.CommandResult;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.operators.rx.RxUtils;
import io.viewserver.reactor.IReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.FuncN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class DataSourceComponents implements IDataSourceServerComponents{
    private IReactor serverReactor;
    private IDataSourceRegistry dataSourceRegistry;
    private IBasicServerComponents basicServerComponents;
    private static final Logger log = LoggerFactory.getLogger(DataSourceComponents.class);

    private static final String DATASOURCE_DEPENDENCY_PREFIX = "/" + IDataSourceRegistry.TABLE_NAME + "/";

    public DataSourceComponents(IBasicServerComponents basicServerComponents) {
        this.basicServerComponents = basicServerComponents;
    }

    @Override
    public IDataSourceRegistry getDataSourceRegistry() {
        return dataSourceRegistry;
    }

    @Override
    public Observable start() {
        log.info("Started data source components");
        dataSourceRegistry = new DataSourceRegistry(basicServerComponents.getServerCatalog(), basicServerComponents.getExecutionContext());
        dataSourceRegistry.getRegistered().subscribe(c-> {
                    log.info("Detected source {} has been registered",c.getName());
                    Set<String> dependencies = this.getDependencies(c);
                    List<String> dependeciesToWaitFor = dependencies.stream().filter(dep -> !this.isBuilt(dataSourceRegistry.get(dep))).collect(Collectors.toList());
                    if(dependeciesToWaitFor.size() > 0){
                        waitFor(c.getName(), dependeciesToWaitFor).subscribeOn(RxUtils.executionContextScheduler(basicServerComponents.getExecutionContext(),1)).subscribe(ds -> runDataSource(c));
                    }
                    else{
                        runDataSource(c);
                    }
        }, err -> log.error("Problem subscribing to registered data sources",err));
        return dataSourceRegistry.getAllBuilt().filter(c-> c).take(1);
    }

    private Observable<Object> waitFor(String dataSourceName, List<String> dependeciesToWaitFor) {
        List<Observable<IDataSource>> observablesToWaitFor = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(String ds : dependeciesToWaitFor){
            if(sb.length() > 0){
                sb.append(",");
            }
            sb.append(ds);
            observablesToWaitFor.add(onDataSourceBuilt(ds));
        }

        log.info(String.format("WAITING - Data sources %s Waiting for %s",dataSourceName,sb));

        FuncN<?> onCompletedAll = new FuncN<Object>() {
            @Override
            public Object call(Object... objects) {
                log.info(String.format("COMPLETED FINISHED WAITING %s for %s ",dataSourceName,sb));
                return true;
            }
        };
        return Observable.zip(observablesToWaitFor, onCompletedAll);
    }

    @Override
    public Observable<Object> onDataSourcesBuilt(String... toWaitFor) {
        List<Observable<IDataSource>> observablesToWaitFor = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(String ds : toWaitFor){
            if(sb.length() > 0){
                sb.append(",");
            }
            sb.append(ds);
            observablesToWaitFor.add(onDataSourceBuilt(ds));
        }

        FuncN<?> onCompletedAll = new FuncN<Object>() {
            @Override
            public Object call(Object... objects) {
                log.info(String.format("COMPLETED FINISHED WAITING %s ",sb));
                return true;
            }
        };
        return Observable.zip(observablesToWaitFor, onCompletedAll);
    }

    public Observable<IDataSource> onDataSourceBuilt(String ds) {
        return dataSourceRegistry.getStatusChanged().filter(c-> c.getName().equals(ds) && isBuilt(dataSourceRegistry.get(ds))).take(1);
    }

    private void runDataSource(final IDataSource dataSource) {
        if (getDependencies(dataSource).stream().anyMatch(c-> !isBuilt(dataSourceRegistry.get(c)))) {
            log.error("You are trying to built a datasource that is still waiting for dependencies aborting");
            return;
        }
        IExecutionContext executionContext = this.basicServerComponents.getExecutionContext();
        executionContext.getReactor().scheduleTask(() -> {
            try {
                CommandResult planResult = new CommandResult();
                planResult.setListener(res -> {
                    if (res.isSuccess()) {
                        log.info("Successfully built data source " + dataSource.getName(), res.getMessage());
                       getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.INITIALIZED);
                    } else {
                        log.error("Problem occurred running execution plan for " + dataSource.getName(), res.getMessage());
                        getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.ERROR);
                    }
                });
                DataSourceHelper.runDataSourceExecutionPlan(this.basicServerComponents.getExecutionPlanRunner(),dataSource, dataSourceRegistry, executionContext,
                        IDataSourceRegistry.getDataSourceCatalog(dataSource, this.basicServerComponents.getServerCatalog()), planResult);
            } catch (Throwable t) {
                log.error("Problem occurred loading data for " + dataSource.getName(), t);
                getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.ERROR);
            }
        }, 1, -1);
    }


    private Set<String> getDependencies(IDataSource dataSource) {
        final Set<String> dependencies = new HashSet<>();
        final List<IGraphNode> nodes = dataSource.getNodes();
        final int nodeCount = nodes.size();
        for (int i = 0; i < nodeCount; i++) {
            final IGraphNode node = nodes.get(i);
            final List<IConfiguratorSpec.Connection> connections = node.getConnections();
            final int connectionCount = connections.size();
            for (int j = 0; j < connectionCount; j++) {
                final IConfiguratorSpec.Connection connection = connections.get(j);
                final String operator = connection.getOperator();
                if (operator.startsWith(DATASOURCE_DEPENDENCY_PREFIX)) {
                    final String suffix = operator.substring(DATASOURCE_DEPENDENCY_PREFIX.length());
                    final int slashIndex = suffix.indexOf("/");
                    if (slashIndex > -1) {
                        final String dependencyName = suffix.substring(0, slashIndex);
                        dependencies.add(dependencyName);
                    }
                }
            }
        }
        return dependencies;
    }
    private boolean isBuilt(IDataSource iDataSource) {
        if(iDataSource == null){
            return false;
        }
        DataSourceStatus status = dataSourceRegistry.getStatus(iDataSource.getName());
        return status.equals(DataSourceStatus.INITIALIZED);
    }
}
