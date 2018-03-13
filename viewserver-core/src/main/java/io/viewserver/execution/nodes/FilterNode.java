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

import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.filter.FilterOperator;
import io.viewserver.operators.filter.IFilterConfig;

import java.util.Map;

/**
 * Created by nickc on 03/11/2014.
 */
public class FilterNode extends GraphNodeBase<FilterNode> {
    private FilterOperator.FilterMode mode = FilterOperator.FilterMode.Transparent;
    private String expression;

    public FilterNode(){
        super();
    }

    public FilterNode(String name) {
        super(name, "Filter");
    }

    public FilterNode withMode(FilterOperator.FilterMode mode) {
        this.mode = mode;
        return this;
    }

    public FilterNode withExpression(String expression) {
        this.expression = expression;
        this.mode = FilterOperator.FilterMode.Filter;
        return this;
    }

    @Override
    public Object getConfig(final ParameterHelper parameterHelper) {
        return new IFilterConfig() {
            @Override
            public FilterOperator.FilterMode getMode() {
                return mode;
            }

            @Override
            public String getExpression() {
                return parameterHelper != null ? parameterHelper.substituteParameterValues(expression) : expression;
            }

            @Override
            public Map<String, String> getColumnAliases() {
                return null;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        return MessagePool.getInstance().get(io.viewserver.messages.config.IFilterConfig.class)
                .setMode(getModeForDto(mode))
                .setExpression(parameterHelper.substituteParameterValues(expression));
    }

    private io.viewserver.messages.config.IFilterConfig.FilterMode getModeForDto(FilterOperator.FilterMode mode) {
        switch (mode) {
            case Transparent: {
                return io.viewserver.messages.config.IFilterConfig.FilterMode.Transparent;
            }
            case Filter: {
                return io.viewserver.messages.config.IFilterConfig.FilterMode.Filter;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unknown mode '%s'", mode.toString()));
            }
        }
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return String.format("filter:%s:%s", parameterHelper.substituteParameterValues(this.expression), this.mode.toString());
    }

    public FilterOperator.FilterMode getMode() {
        return mode;
    }

    public String getExpression() {
        return expression;
    }

    public void setMode(FilterOperator.FilterMode mode) {
        this.mode = mode;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "FilterNode{" +
                "mode=" + mode +
                ", expression='" + expression + '\'' +
                '}';
    }
}
