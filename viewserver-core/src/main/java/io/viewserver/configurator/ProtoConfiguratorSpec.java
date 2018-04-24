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


import io.viewserver.messages.command.IConfigurateCommand;
import io.viewserver.operators.IConfigurableOperatorFactory;
import io.viewserver.operators.IOperatorFactory;
import io.viewserver.operators.OperatorFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bemm on 31/10/2014.
 */

public class ProtoConfiguratorSpec implements IConfiguratorSpec {
    private static final Logger log = LoggerFactory.getLogger(ProtoConfiguratorSpec.class);
    private final OperatorSpec[] operators;

    public ProtoConfiguratorSpec(IConfigurateCommand configuratorMessage, OperatorFactoryRegistry operatorFactoryRegistry) {
        final List<IConfigurateCommand.IOperator> operatorsList = configuratorMessage.getOperators();
        final int operatorCount = operatorsList.size();
        this.operators = new OperatorSpec[operatorCount];
        for (int i = 0; i < operatorCount; i++) {
            IConfigurateCommand.IOperator operator = operatorsList.get(i);

            IOperatorFactory operatorFactory = operatorFactoryRegistry.get(operator.getType());
            Object configWrapper = null;
            if (operatorFactory instanceof IConfigurableOperatorFactory) {
                if (operator.hasConfig()) {
                    final Class configClass = ((IConfigurableOperatorFactory) operatorFactory).getProtoConfigDtoClass();
                    Object config = operator.getConfig(configClass);
                    configWrapper = ((IConfigurableOperatorFactory) operatorFactory).getProtoConfigWrapper(config);
                }
            }

            OperatorSpec operatorSpec = new OperatorSpec(operator.getName(), operator.getType(), getOperation(operator.getOperation()), false, configWrapper);
            List<IConfigurateCommand.IMetadataItem> metadataList = operator.getMetadataItems();
            int metadataCount = metadataList.size();
            for (int j = 0; j < metadataCount; j++) {
                IConfigurateCommand.IMetadataItem metadataItem = metadataList.get(j);
                try {
                    Object value;
                    switch (metadataItem.getType()) {
                        case Boolean: {
                            value = metadataItem.getBooleanValue();
                            break;
                        }
                        case Integer: {
                            value = metadataItem.getIntegerValue();
                            break;
                        }
                        case Long: {
                            value = metadataItem.getLongValue();
                            break;
                        }
                        case Float: {
                            value = metadataItem.getFloatValue();
                            break;
                        }
                        case Double: {
                            value = metadataItem.getDoubleValue();
                            break;
                        }
                        case String: {
                            value = metadataItem.getStringValue();
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("Unknown type of metadata item in dto");
                        }
                    }
                    operatorSpec.addMetadata(metadataItem.getKey(), value);
                } finally {
                    metadataItem.release();
                }
            }
            List<IConfigurateCommand.IConnection> connectionsList = operator.getConnections();
            int connectionsCount = connectionsList.size();
            for (int j = 0; j < connectionsCount; j++) {
                IConfigurateCommand.IConnection connection = connectionsList.get(j);
                operatorSpec.getConnections().add(new Connection(connection.getOperator(), connection.getOutput(), connection.getInput()));
                connection.release();
            }

            this.operators[i] = operatorSpec;
            operator.release();
        }
    }

    private OperatorSpec.Operation getOperation(IConfigurateCommand.Operation operation) {
        switch (operation) {
            case CreateConfigure: {
                return OperatorSpec.Operation.CreateConfigure;
            }
            case Remove: {
                return OperatorSpec.Operation.Remove;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown operation '%s'", operation));
            }
        }
    }

    @Override
    public java.util.List<OperatorSpec> getOperators() {
        return Arrays.asList(operators);
    }

    @Override
    public void reset() {
    }
}
