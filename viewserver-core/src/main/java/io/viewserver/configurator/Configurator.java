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

package io.viewserver.configurator;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.command.MultiCommandResult;
import io.viewserver.configurator.IConfiguratorSpec.OperatorSpec.Operation;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.util.ViewServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by nickc on 20/10/2014.
 */
public class Configurator implements IConfigurator {
    private static final Logger log = LoggerFactory.getLogger(Configurator.class);
    private final OperatorFactoryRegistry operatorFactoryRegistry;

    public Configurator(OperatorFactoryRegistry operatorFactoryRegistry) {
        this.operatorFactoryRegistry = operatorFactoryRegistry;
    }

    @Override
    public void process(IConfiguratorSpec configurator, IExecutionContext executionContext, ICatalog catalog, CommandResult commandResult) {
        ConfiguratorState state = new ConfiguratorState(configurator, executionContext, catalog, commandResult);

        try {

            queueOperatorsForRemoval(state);
            log.debug("Queued {} operators from removal", state.operatorsToRemove.size());

            createOperators(state);
            log.debug("Created {} operators", state.newOperators.size());

            connectOperators(state);
            log.debug("Finished connecting operators");
            configureOperators(state);
            log.debug("Finished configuring operators");
            tearDownOperators(state);
            log.debug("Finished tearing down operators");
        } catch (Throwable e) {
            rollback(state);
            throw e;
        }
    }

    private void rollback(ConfiguratorState state) {
        for (IOperator newOperator : state.newOperators) {
            newOperator.tearDown();
        }
    }

    private void queueOperatorsForRemoval(ConfiguratorState state) {
        List<IConfiguratorSpec.OperatorSpec> operators = state.configurator.getOperators();
        int count = operators.size();
        for (int i = 0; i < count; i++) {
            IConfiguratorSpec.OperatorSpec operator = operators.get(i);
            if (!operator.getOperation().equals(Operation.Remove)) {
                continue;
            }

            IOperator operatorToRemove = state.catalog.getOperatorByPath(operator.getName());
            if (operatorToRemove != null) {
                state.operatorsToRemove.add(operatorToRemove);
            }
        }
    }

    private void createOperators(ConfiguratorState state) {
        List<IConfiguratorSpec.OperatorSpec> operators = state.configurator.getOperators();
        int count = operators.size();
        for (int i = 0; i < count; i++) {
            IConfiguratorSpec.OperatorSpec operatorConfig = operators.get(i);
            if (operatorConfig.getOperation() != Operation.CreateConfigure) {
                continue;
            }

            IOperatorFactory operatorFactory = operatorFactoryRegistry.get(operatorConfig.getType());
            if (operatorFactory == null) {
                throw new ViewServerException("No registered operator factory can create operators of type '" + operatorConfig.getType() + "'");
            }

            state.operatorSpec = operatorConfig;

            String name = operatorConfig.getName();
            IOperator operator = state.catalog.getOperatorByPath(name);
            if (operator != null) {
                if (operatorFactory.getOperatorClass() != null &&
                        !operator.getClass().equals(operatorFactory.getOperatorClass())) {
                    throw new ViewServerException("An operator called '" + name + "' already exists, but is of a different type.");
                }
            } else {
                final int lastSlash = name.lastIndexOf("/");
                ICatalog tempCatalog = state.catalog;
                if (lastSlash != -1) {
                    String parent = lastSlash == 0 ? "/" : name.substring(0, lastSlash);
                    state.catalog = (ICatalog) state.catalog.getOperatorByPath(parent);
                    name = name.substring(lastSlash + 1);
                }
                operator = operatorFactory.createOperator(name, state,  operatorConfig.getConfig());
                state.catalog = tempCatalog;
                state.newOperators.add(operator);
            }

            setMetadataConsumer.operator = operator;
            operatorConfig.getMetadata().entrySet().forEach(setMetadataConsumer.proc);
            setMetadataConsumer.operator = null;

            if (operatorFactory instanceof IInputConfigurator && operatorConfig.getConfig() instanceof IInputConfig) {
                ((IInputConfigurator)operatorFactory).configureInputs(operator, operatorConfig.getConfig());
            }
            if (operatorFactory instanceof IOutputConfigurator && operatorConfig.getConfig() instanceof IOutputConfig) {
                ((IOutputConfigurator)operatorFactory).configureOutputs(operator, operatorConfig.getConfig());
            }
        }
    }

    private class SetMetadataConsumer {
        private IOperator operator;
        private final Consumer<? super Map.Entry<String, Object>> proc = metadataItem -> {
            operator.setMetadata(metadataItem.getKey(), metadataItem.getValue());
        };
    }
    private final SetMetadataConsumer setMetadataConsumer = new SetMetadataConsumer();

    private void connectOperators(ConfiguratorState state) {
        List<IConfiguratorSpec.OperatorSpec> operators = state.configurator.getOperators();
        int count = operators.size();
        for (int i = 0; i < count; i++) {
            IConfiguratorSpec.OperatorSpec operatorConfig = operators.get(i);
            if (operatorConfig.getOperation() != Operation.CreateConfigure) {
                continue;
            }

            IOperator targetOperator = state.catalog.getOperatorByPath(operatorConfig.getName());

            List<IConfiguratorSpec.Connection> connections = operatorConfig.getConnections();
            int connectionsCount = connections.size();
            for (int j = 0; j < connectionsCount; j++) {
                IConfiguratorSpec.Connection connection = connections.get(j);
                IOperator sourceOperator = state.catalog.getOperatorByPath(connection.getOperator());
                if (sourceOperator == null) {
                    throw new ViewServerException("Source operator '" + connection.getOperator() + "' does not exist");
                }

                IOutput output = sourceOperator.getOutput(connection.getOutput());
                if (output == null) {
                    throw new ViewServerException("Output '" + connection.getOutput() + "' does not exist on operator '" + connection.getOperator() + "'");
                }

                IInput input = targetOperator.getInput(connection.getInput());
                if (input == null) {
                    throw new ViewServerException("Input '" + connection.getInput() + "' does not exist on operator '" + targetOperator.getName() + "'");
                }

                if (input.getProducer() != null) {
                    if (input.getProducer() != output) {
                        if (connection.getUnplugExisting()) {
                            input.getProducer().unplug(input);
                        } else {
                            throw new ViewServerException("Another output is already plugged in to input '" + connection.getInput() + "' on operator '" + targetOperator.getName() + "'");
                        }
                    } else {
                        // already plugged in
//                        log.debug(((InputBase) input).getOwner().getName() + " input already plugged in");
                        continue;
                    }
                }

                log.debug("connecting output {} to input {}", output.getFullName(), input.getFullName());

                output.plugIn(input);
            }
        }
    }

    private void configureOperators(ConfiguratorState state) {
        List<IConfiguratorSpec.OperatorSpec> operators = state.configurator.getOperators();
        ArrayList<CommandResult> commandResults = new ArrayList<>();
        int count = operators.size();
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0, j = 0; i < count; i++) {
                IConfiguratorSpec.OperatorSpec operatorConfig = operators.get(i);
                if (operatorConfig.getOperation() != Operation.CreateConfigure) {
                    continue;
                }

                IOperator operator = state.catalog.getOperatorByPath(operatorConfig.getName());
                IOperatorFactory operatorFactory = operatorFactoryRegistry.get(operatorConfig.getType());
                if (!(operatorFactory instanceof IConfigurableOperatorFactory)) {
                    continue;
                }

                if (pass == 0) {
                    CommandResult resultForDependency = state.configureResult.getResultForDependency("Configure operator '" + operatorConfig.getName() + "'");
                    commandResults.add(resultForDependency);
                } else {
                    log.debug("Configuring operator {} operators", operator.getName());
                    ((IConfigurableOperatorFactory) operatorFactory).configureOperator(operator, operatorConfig.getConfig(), state, commandResults.get(j++));
                }
            }
        }
    }

    private void tearDownOperators(ConfiguratorState state) {
        for (IOperator operator : state.operatorsToRemove) {
            operator.tearDown();
        }
    }

    public static class ConfiguratorState {
        public final IConfiguratorSpec configurator;
        public IConfiguratorSpec.OperatorSpec operatorSpec;
        public ICatalog catalog;
        private final Set<IOperator> operatorsToRemove = new HashSet<>();
        public final IExecutionContext executionContext;
        public final Set<IOperator> newOperators = new HashSet<>();
        public MultiCommandResult configureResult;

        public ConfiguratorState(IConfiguratorSpec configurator, IExecutionContext executionContext, ICatalog catalog, CommandResult commandResult) {
            this.configurator = configurator;
            this.executionContext = executionContext;
            this.catalog = catalog;

            this.configureResult = MultiCommandResult.wrap("configurator", commandResult);
        }

    }
}
