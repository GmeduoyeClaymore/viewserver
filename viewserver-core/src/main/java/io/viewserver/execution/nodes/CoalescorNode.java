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

import io.viewserver.command.CommandResult;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.Hasher;
import io.viewserver.distribution.ICoalescorConfig;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.group.IGroupByConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickc on 26/11/2014.
 */
public class CoalescorNode extends GraphNodeBase<CoalescorNode> {
    private final IGraphNode targetNodeCopy;
    private String countColumnName;
    private List<IGroupByConfig.Summary> summaries;
    private boolean isGroupBy;
    private List<String> groupByColumns;
    private IGraphNode targetNode;
    private CommandResult remoteConfigurationResult;

    public CoalescorNode(String name, ICoalesceableGraphNode targetNode, ParameterHelper parameterHelper, CommandResult remoteConfigurationResult) {
        super(name, "Coalescor");
        this.targetNode = targetNode;
        this.remoteConfigurationResult = remoteConfigurationResult;
        targetNodeCopy = targetNode.copyForCoalescing();

        isGroupBy = (targetNode instanceof GroupByNode);
        if (isGroupBy) {
            IGroupByConfig config = (IGroupByConfig) this.targetNode.getConfig(parameterHelper);

            groupByColumns = config.getGroupBy();

            summaries = new ArrayList<>(config.getSummaries());

            countColumnName = config.getCountColumnName();
        }
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new ICoalescorConfig() {
            @Override
            public boolean isGroupBy() {
                return isGroupBy;
            }

            @Override
            public List<String> getGroupBy() {
                return groupByColumns;
            }

            @Override
            public List<Summary> getSummaries() {
                return summaries;
            }

            @Override
            public String getCountColumnName() {
                return countColumnName != null ? countColumnName : "count";
            }

            @Override
            public List<String> getSubtotals() {
                return null;
            }

            @Override
            public String getTarget() { return targetNode.getNameForOperatorSpec(true); }

            @Override
            public CommandResult getRemoteConfigurationResult() {
                return remoteConfigurationResult;
            }
        };
    }

    @JsonIgnore
    @Override
    public String getNameForOperatorSpec(boolean shouldHash) {
        if (operatorName == null) {
            operatorName = Hasher.SHA1(String.format("coalescor: {target: \"%s\"}", targetNode.getNameForOperatorSpec(true)));
        }
        return operatorName;
//        return Hasher.SHA1(String.format("coalescor: {%s}", targetNodeCopy.getOperatorName(parameterHelper)));
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        return null;
    }

    @Override
    public CoalescorNode withConnection(String operator, String output, String input) {
        super.withConnection(operator, output, input);
        List<IConfiguratorSpec.Connection> connections = getConnections();
        connections.get(connections.size() - 1).setUnplugExisting(true);
        return this;
    }
}
