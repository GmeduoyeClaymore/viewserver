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

package io.viewserver.execution.nodes;

import io.viewserver.Constants;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.Hasher;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.command.IConfigurateCommand;
import io.viewserver.messages.config.IOperatorConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 01/11/2014.
 */
public abstract class GraphNodeBase<TNode> implements IGraphNode {
    private String name;
    private String type;
    private List<IConfiguratorSpec.Connection> connections = new ArrayList<>();
    private boolean distributed;
    private Map<String, Object> metadata;
    private ParameterHelper parameterHelper;
    protected String operatorName;

    protected GraphNodeBase() {
    }

    ;

    protected GraphNodeBase(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setParameterHelper(ParameterHelper parameterHelper) {
        this.parameterHelper = parameterHelper;
    }

    @Override
    @JsonIgnore
    public String getOperatorName(ParameterHelper parameterHelper) {
        if (name.startsWith("/")) {
            return name;
        }

        StringBuilder builder = new StringBuilder();
        builder.append('{');
        builder.append("name: \"").append(name).append('"');
        builder.append(",type: \"").append(type).append('"');
        if (!connections.isEmpty()) {
            builder.append(",inputs: [");
            boolean first = true;
            int count = connections.size();
            for (int i = 0; i < count; i++) {
                IConfiguratorSpec.Connection connection = connections.get(i);
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append("{");
                builder.append("input:\"").append(connection.getInput()).append('"');
                builder.append(",operator:\"").append(connection.getOperator()).append('"');
                builder.append(",output:\"").append(connection.getOutput()).append('"');
                builder.append("}");
            }
            builder.append("]");
        }
        String config = getConfigForOperatorName(parameterHelper);
        if (config != null) {
            builder.append(",config:").append(config);
        }
        builder.append('}');
        return builder.toString();
    }

    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return null;
    }

    @Override
    public List<IConfiguratorSpec.Connection> getConnections() {
        return connections;
    }

    @Override
    public TNode withMetadata(Map<String, Object> metadata) {
        ensureMetadata();
        this.metadata.putAll(metadata);
        return (TNode) this;
    }

    @Override
    public TNode withMetadata(String key, Object value) {
        ensureMetadata();
        this.metadata.put(key, value);
        return (TNode) this;
    }

    private void ensureMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }

    @Override
    public TNode withConnection(String operator) {
        return withConnection(operator, Constants.OUT, Constants.IN);
    }

    @Override
    public TNode withConnection(String operator, String output, String input) {
        if (operator != null) {
            connections.add(new IConfiguratorSpec.Connection(operator, output, input));
        }
        return (TNode) this;
    }

    @JsonIgnore
    @Override
    public List<String> getDependencies() {
        ArrayList<String> deps = new ArrayList<>();
        int count = connections.size();
        for (int i = 0; i < count; i++) {
            deps.add(connections.get(i).getOperator());
        }
        return deps;
    }

    @Override
    public IConfiguratorSpec.OperatorSpec getOperatorSpec(ParameterHelper parameterHelper, boolean shouldHash) {
        String name = getNameForOperatorSpec(shouldHash);
        IConfiguratorSpec.OperatorSpec operatorSpec = new IConfiguratorSpec.OperatorSpec(name, type, IConfiguratorSpec.OperatorSpec.Operation.CreateConfigure,
                distributed, getConfig(parameterHelper));
        if (metadata != null) {
            operatorSpec.addMetadata(metadata);
        }
        int count = connections.size();
        for (int i = 0; i < count; i++) {
            operatorSpec.getConnections().add(connections.get(i));
        }
        return operatorSpec;
    }

    @Override
    public IConfigurateCommand.IOperator getOperatorSpecDto(ParameterHelper parameterHelper) {
        String name = getNameForOperatorSpec(true);
        IConfigurateCommand.IOperator operatorDto = MessagePool.getInstance().get(IConfigurateCommand.IOperator.class)
                .setName(name)
                .setType(type)
                .setOperation(IConfigurateCommand.Operation.CreateConfigure);
        IOperatorConfig configDto = getConfigDto(parameterHelper);
        if (configDto != null) {
            operatorDto.setConfig(configDto);
        }
        if (metadata != null) {
            metadata.entrySet().forEach(metadataItem -> {
                IConfigurateCommand.IMetadataItem metadataItemBuilder = MessagePool.getInstance().get(IConfigurateCommand.IMetadataItem.class)
                        .setKey(metadataItem.getKey());
                if (metadataItem.getValue() instanceof Boolean) {
                    metadataItemBuilder.setBooleanValue((boolean) metadataItem.getValue());
                } else if (metadataItem.getValue() instanceof Byte || metadataItem.getValue() instanceof Short || metadataItem.getValue() instanceof Integer) {
                    metadataItemBuilder.setIntegerValue((int) metadataItem.getValue());
                } else if (metadataItem.getValue() instanceof Long) {
                    metadataItemBuilder.setLongValue((long) metadataItem.getValue());
                } else if (metadataItem.getValue() instanceof Float) {
                    metadataItemBuilder.setFloatValue((float) metadataItem.getValue());
                } else if (metadataItem.getValue() instanceof Double) {
                    metadataItemBuilder.setDoubleValue((double) metadataItem.getValue());
                } else if (metadataItem.getValue() instanceof String) {
                    metadataItemBuilder.setStringValue((String) metadataItem.getValue());
//                } else if (metadataItem.getValue() instanceof byte[]) {
//                    metadataItemBuilder.setBlobValue(ByteString.copyFrom((byte[]) metadataItem.getValue()));
                }
                operatorDto.getMetadataItems().add(metadataItemBuilder);
                metadataItemBuilder.release();
            });
        }
        int count = connections.size();
        for (int i = 0; i < count; i++) {
            IConfiguratorSpec.Connection connection = connections.get(i);
            final IConfigurateCommand.IConnection connectionMessage = MessagePool.getInstance().get(IConfigurateCommand.IConnection.class)
                    .setInput(connection.getInput())
                    .setOperator(connection.getOperator())
                    .setOutput(connection.getOutput());
            operatorDto.getConnections().add(connectionMessage);
            connectionMessage.release();
        }
        return operatorDto;
    }

    @JsonIgnore
    @Override
    public String getNameForOperatorSpec(boolean shouldHash) {
        if (this.operatorName == null) {
            this.operatorName = (shouldHash && name.charAt(0) != '/') ? Hasher.SHA1(getOperatorName(parameterHelper)) : name;
        }
        return this.operatorName;
    }

    protected abstract IOperatorConfig getConfigDto(ParameterHelper parameterHelper);

    @Override
    public boolean isDistributed() {
        return distributed;
    }

    @Override
    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }

    @Override
    public TNode withDistribution() {
        distributed = true;
        return (TNode) this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setConnections(List<IConfiguratorSpec.Connection> connections) {
        this.connections = connections;
    }

    /*
        private String name;
    private String type;
    private List<IConfiguratorSpec.Connection> connections = new ArrayList<>();
    private boolean distributed;
    private Map<String, Object> metadata;
     */
    public void copyTo(GraphNodeBase<TNode> copy) {
        copy.setName(name);
        copy.setType(type);
        copy.setDistributed(distributed);
        copy.setConnections(connections);
    }

    @Override
    public String toString() {
        return getOperatorName(parameterHelper);
    }
}
