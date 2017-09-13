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

package io.viewserver.execution.context;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.core.ExecutionContext;
import io.viewserver.datasource.IDataSource;
import io.viewserver.distribution.IDistributionManager;
import io.viewserver.distribution.ViewServerNode;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.execution.nodes.IGraphNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 01/11/2014.
 */
public interface IExecutionPlanContext {
    ICatalog getCatalog();

    ExecutionContext getExecutionContext();

    void setCatalog(ICatalog catalog);

    void setInput(String operator);

    void setInput(String operator, String output);

    String getInputOperator();

    String getInputOutputName();

    void setExecutionContext(ExecutionContext executionContext);

    void setAggregating(boolean aggregating);

    boolean isAggregating();

    ParameterHelper getParameterHelper();

    void addNodes(IGraphNode... graphNodes);

    void addNodes(Collection<? extends IGraphNode> graphNodes);

    List<IGraphNode> getGraphNodes();

    void clearNodes();

    void setDistributionManager(IDistributionManager distributionManager);

    IDistributionManager getDistributionManager();

    IDataSource getDataSource();

    void setDataSource(IDataSource dataSource);

    void setOperatorName(String nodeName, String operatorName);

    String getOperatorName(String nodeName);

    CommandResult getRemoteConfigurationResult();

    void setRemoteConfigurationResult(CommandResult remoteConfigurationResult);

    boolean isDistributed();

    boolean shouldHashNames();

    Map<String, String> getDefaultNodeNames();

    void setViewServerNodes(List<ViewServerNode> viewServerNodes);

    List<ViewServerNode> getViewServerNodes();
}
