package io.viewserver.server;

import io.viewserver.command.CommandResult;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.datasource.*;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.reactor.IReactor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
    public void start() {
        dataSourceRegistry = new DataSourceRegistry(basicServerComponents.getServerCatalog(), basicServerComponents.getExecutionContext(),DataSource.class);
        dataSourceRegistry.addListener(new IDataSourceListener() {
            @Override
            public void onDataSourceRegistered(IDataSource dataSource) {
            }

            @Override
            public void onDataSourceStatusChanged(IDataSource dataSource, DataSourceStatus status) {
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
                                final DataSourceStatus dependencyStatus = dataSourceRegistry.getStatus(dependencyName);
                                if (status != DataSourceStatus.BUILT && dependencyStatus != DataSourceStatus.INITIALIZED) {
                                    dependencies.add(dependencyName);
                                }
                            }
                        }
                    }
                }
                if (dependencies.contains(dataSource.getName()) && (status == DataSourceStatus.BUILT
                        || status == DataSourceStatus.INITIALIZED)) {
                    dependencies.remove(dataSource.getName());
                    if (dependencies.isEmpty()) {
                        runDataSource(dataSource);
                    }
                }
            }
        });
    }

    private void runDataSource(final IDataSource dataSource) {
        this.basicServerComponents.getExecutionContext().getReactor().scheduleTask(() -> {
            try {
                if (!checkDependencies(dataSource)) {
                    return;
                }

                getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.INITIALIZING);
                CommandResult planResult = new CommandResult();
                planResult.setListener(res -> {
                    if (res.isSuccess()) {
                        getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.INITIALIZED);
                    } else {
                        log.error("Problem occurred loading data for " + dataSource.getName(), res.getMessage());
                        getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.ERROR);
                    }
                });
                DataSourceHelper.runDataSourceExecutionPlan(dataSource, dataSourceRegistry, this.basicServerComponents.getExecutionContext(),
                        IDataSourceRegistry.getDataSourceCatalog(dataSource, this.basicServerComponents.getServerCatalog()), planResult);
            } catch (Throwable t) {
                log.error("Problem occurred loading data for " + dataSource.getName(), t);
                getDataSourceRegistry().setStatus(dataSource.getName(), DataSourceStatus.ERROR);
            }
        }, 0, -1);
    }

    private boolean checkDependencies(IDataSource dependingDataSource) {
        final Set<String> dependencies = new HashSet<>();
        final List<IGraphNode> nodes = dependingDataSource.getNodes();
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
                        final DataSourceStatus status = dataSourceRegistry.getStatus(dependencyName);
                        if (status != DataSourceStatus.BUILT && status != DataSourceStatus.INITIALIZED) {
                            dependencies.add(dependencyName);
                        }
                    }
                }
            }
        }
        if (dependencies.isEmpty()) {
            log.debug("{} has no outstanding dependencies - will build", dependingDataSource.getName());
            return true;
        }


        if (log.isDebugEnabled()) {
            log.debug("{} has outstanding dependencies - {} - not building now", dependingDataSource.getName(),
                    StringUtils.join(dependencies, ','));
        }
        return false;
    }



}
