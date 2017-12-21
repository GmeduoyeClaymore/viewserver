/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.command;

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.messages.command.IInitialiseSlaveCommand;
import io.viewserver.operators.IOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by nick on 15/10/15.
 */
public class Resetter {
    private static final Logger log = LoggerFactory.getLogger(Resetter.class);
    private ICatalog systemCatalog;
    private IDataSourceRegistry<? extends IDataSource> dataSourceRegistry;
    private IDistributionManager distributionManager;
    private IExecutionContext executionContext;
    private IDimensionMapper dimensionMapper;

    public Resetter(ICatalog systemCatalog, IDataSourceRegistry dataSourceRegistry,
                    IDistributionManager distributionManager, IExecutionContext executionContext,
                    IDimensionMapper dimensionMapper) {
        this.systemCatalog = systemCatalog;
        this.dataSourceRegistry = dataSourceRegistry;
        this.distributionManager = distributionManager;
        this.executionContext = executionContext;
        this.dimensionMapper = dimensionMapper;
    }

    public void reset(CommandResult commandResult, boolean clearDataSources) {
        try {
            tearDownOperators(systemCatalog.getAllOperators());

            MultiCommandResult multiCommandResult = MultiCommandResult.wrap("ResetCommandHandler", commandResult);

            if (clearDataSources) {
                dataSourceRegistry.clear();
                dimensionMapper.clear();
            } else {
                for (IDataSource dataSource : dataSourceRegistry.getAll()) {
                    // don't reset system data sources
                    if (dataSource.hasOption(DataSourceOption.IsSystem)) {
                        continue;
                    }

                    CommandResult dataSourceResult = multiCommandResult.getResultForDependency("Data source '" + dataSource.getName() + "'");
                    DataSourceHelper.runDataSourceExecutionPlan(dataSource, dataSourceRegistry, executionContext,
                            IDataSourceRegistry.getDataSourceCatalog(dataSource, systemCatalog), distributionManager, dataSourceResult);
                }
            }

            CommandResult commitResult = multiCommandResult.getResultForDependency("Commit");
            executionContext.getReactor().addLoopTask(new Runnable() {
                @Override
                public void run() {
                    executionContext.getReactor().removeLoopTask(this);
                    commitResult.setSuccess(true).setComplete(true);
                }
            });

            if (distributionManager.getNodeType().equals(IInitialiseSlaveCommand.Type.Master)) {
                CommandResult nodesResult = multiCommandResult.getResultForDependency("Reset nodes");
                distributionManager.resetAllNodes(nodesResult);
            }
        } catch (Throwable ex) {
            log.error("Failed to reset", ex);
            commandResult.setSuccess(false).setMessage(ex.getMessage()).setComplete(true);
        }
    }

    private void tearDownOperators(Collection<IOperator> operatorsToTearDown) {
        List<IOperator> operators = new ArrayList<>(operatorsToTearDown);
        for (IOperator operator : operators) {
            if (operator instanceof ICatalog) {
                if (operator instanceof DataSourceRegistryBase.DataSourceCatalog) {
                    continue;
                }
                tearDownOperators(((ICatalog)operator).getAllOperators());
            }

            if (operator.isSystemOperator()) {
                continue;
            }

            operator.tearDown();
        }
    }
}
