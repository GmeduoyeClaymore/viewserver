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
import io.viewserver.operators.spread.ISpreadConfig;

import java.util.Map;

/**
 * Created by bemm on 03/11/2014.
 */
public class SpreadNode extends GraphNodeBase<SpreadNode> {

    private String inputColumn;
    private String outputColumn;
    private String spreadFunction;
    private boolean removeInputColumn;

    public SpreadNode(){
        super();
    }

    public SpreadNode(String name) {
        super(name, "Spread");
    }


    public SpreadNode withInputColumn(String inputColumn) {
        this.inputColumn = inputColumn;
        return this;
    }

    public SpreadNode withOutputColumn(String outputColumn) {
        this.outputColumn = outputColumn;
        return this;
    }

    public SpreadNode withSpreadFunction(String spreadFunction) {
        this.spreadFunction = spreadFunction;
        return this;
    }

    @Override
    public Object getConfig(final ParameterHelper parameterHelper) {
        return new ISpreadConfig() {
            @Override
            public String getInputColumnName() {
                return parameterHelper.substituteParameterValues(inputColumn);
            }

            @Override
            public String getOutputColumnName() {
                return parameterHelper.substituteParameterValues(outputColumn);
            }

            @Override
            public String spreadFunctionName() {
                return parameterHelper.substituteParameterValues(spreadFunction);
            }

            @Override
            public boolean removeInputColumn() {
                return removeInputColumn;
            }

        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        return MessagePool.getInstance().get(io.viewserver.messages.config.ISpreadConfig.class)
                .setInputColumnName(parameterHelper.substituteParameterValues(inputColumn))
                .setOutputColumnName(parameterHelper.substituteParameterValues(outputColumn))
                .setSpreadFunctionName(parameterHelper.substituteParameterValues(spreadFunction));
    }


    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return String.format("spread:%s:%s:%s", parameterHelper.substituteParameterValues(this.inputColumn), parameterHelper.substituteParameterValues(this.outputColumn), parameterHelper.substituteParameterValues(this.spreadFunction));
    }

    public String getInputColumn() {
        return inputColumn;
    }

    public String getOutputColumn() {
        return outputColumn;
    }

    public String getSpreadFunction() {
        return spreadFunction;
    }

    @Override
    public String toString() {
        return "SpreadNode{" +
                "inputColumn='" + inputColumn + '\'' +
                ", outputColumn='" + outputColumn + '\'' +
                ", spreadFunction='" + spreadFunction + '\'' +
                '}';
    }
}
