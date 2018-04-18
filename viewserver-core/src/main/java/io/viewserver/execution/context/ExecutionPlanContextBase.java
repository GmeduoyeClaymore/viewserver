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

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.IDataSource;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.execution.nodes.IGraphNode;
import io.viewserver.expression.IExpressionParser;

import java.util.*;

/**
 * Created by nickc on 01/11/2014.
 */
public abstract class ExecutionPlanContextBase implements IExecutionPlanContext {
    private ICatalog catalog;
    private String inputOperator;
    private String inputOutputName;
    private IExecutionContext executionContext;
    private ParameterHelper parameterHelper;
    private List<IGraphNode> graphNodes = new ArrayList<>();
    private IDataSource dataSource;
    private IExpressionParser expressionParser;
    private Map<String, String> operatorNames = new HashMap<>();

    @Override
    public ICatalog getCatalog() {
        return catalog;
    }

    @Override
    public void setCatalog(ICatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public void setInput(String operator) {
        setInput(operator, Constants.OUT);
    }

    @Override
    public void setInput(String operator, String output) {
        this.inputOperator = operator;
        this.inputOutputName = output;
    }

    @Override
    public String getInputOperator() {
        return inputOperator;
    }

    @Override
    public String getInputOutputName() {
        return inputOutputName;
    }

    @Override
    public IExecutionContext getExecutionContext() {
        return executionContext;
    }

    @Override
    public void setExecutionContext(IExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public void setParameterHelper(ParameterHelper parameterHelper) {
        this.parameterHelper = parameterHelper;
    }

    @Override
    public ParameterHelper getParameterHelper() {
        return parameterHelper;
    }

    @Override
    public void addNodes(IGraphNode... graphNodes) {
        this.graphNodes.addAll(Arrays.asList(graphNodes));
    }

    @Override
    public void addNodes(Collection<? extends IGraphNode> graphNodes) {
        this.graphNodes.addAll(graphNodes);
    }

    @Override
    public List<IGraphNode> getGraphNodes() {
        return graphNodes;
    }

    @Override
    public void clearNodes() {
        graphNodes.clear();
    }

    @Override
    public IDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(IDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void setOperatorName(String nodeName, String operatorName) {
        operatorNames.put(nodeName, operatorName);
    }

    @Override
    public String getOperatorName(String nodeName) {
        return operatorNames.get(nodeName);
    }

    @Override
    public boolean shouldHashNames() {
        return true;
    }

    @Override
    public Map<String, String> getDefaultNodeNames() {
        return new HashMap<>();
    }

    public IExpressionParser getExpressionParser() {
        return expressionParser;
    }

    public void setExpressionParser(IExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

}
