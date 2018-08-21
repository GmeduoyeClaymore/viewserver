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
import io.viewserver.operators.transpose.ITransposeConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bemm on 03/11/2014.
 */
public class TransposeNode extends GraphNodeBase<TransposeNode> {
    private List<String> keyColumns = new ArrayList<>();
    private String pivotColumn;
    private List<Object> pivotValues = new ArrayList<>();
    private io.viewserver.execution.nodes.IPivotValueProvider pivotValueProvider;

    public TransposeNode(){super();}

    public TransposeNode(String name) {
        super(name, "Transpose");
    }

    public TransposeNode withKeyColumns(String... keyColumns) {
        this.keyColumns.addAll(Arrays.asList(keyColumns));
        return this;
    }

    public TransposeNode withPivotColumn(String pivotColumn) {
        this.pivotColumn = pivotColumn;
        return this;
    }

    public TransposeNode withPivotValues(Object... pivotValues) {
        this.pivotValues.addAll(Arrays.asList(pivotValues));
        return this;
    }

    public TransposeNode withPivotValueProvider(io.viewserver.execution.nodes.IPivotValueProvider pivotValueProvider) {
        this.pivotValueProvider = pivotValueProvider;
        return this;
    }

    @Override
    public Object getConfig(final ParameterHelper parameterHelper) {
        return new ITransposeConfig() {
            @Override
            public List<String> getKeyColumns() {
                List<String> groupBy = new ArrayList<>();
                for (String column : TransposeNode.this.keyColumns) {
                    String values = parameterHelper.substituteParameterValues(column);

                    if(values!=null) {
                        groupBy.addAll(Arrays.asList(values.split(",")));
                    }

                }
                return groupBy;
            }

            @Override
            public String getPivotColumn() {
                return parameterHelper.substituteParameterValues(pivotColumn);
            }

            @Override
            public Object[] getPivotValues() {
                if (pivotValueProvider != null) {
                    return pivotValueProvider.getPivotValues(parameterHelper);
                }
                return pivotValues.toArray();
            }
        };
    }

    public List<String> getKeyColumns() {
        return keyColumns;
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        io.viewserver.messages.config.ITransposeConfig transposeConfigDto = MessagePool.getInstance().get(io.viewserver.messages.config.ITransposeConfig.class)
                .setPivotColumn(pivotColumn);
        transposeConfigDto.getKeyColumns().addAll(keyColumns);
        Object[] pivotValues = pivotValueProvider != null ? pivotValueProvider.getPivotValues(parameterHelper) : this.pivotValues.toArray();
        int count = pivotValues.length;
        final List<String> pivotValuesList = transposeConfigDto.getPivotValues();
        for (int i = 0; i < count; i++) {
            pivotValuesList.add(pivotValues[i].toString());
        }
        return transposeConfigDto;
    }

    public interface IPivotValueProvider {
        Object[] getPivotValues(ParameterHelper parameterHelper);
    }

    public String getPivotColumn() {
        return pivotColumn;
    }

    public List<Object> getPivotValues() {
        return pivotValues;
    }

    public io.viewserver.execution.nodes.IPivotValueProvider getPivotValueProvider() {
        return pivotValueProvider;
    }

    public void setKeyColumns(List<String> keyColumns) {
        this.keyColumns = keyColumns;
    }

    public void setPivotColumn(String pivotColumn) {
        this.pivotColumn = pivotColumn;
    }

    public void setPivotValues(List<Object> pivotValues) {
        this.pivotValues = pivotValues;
    }

    public void setPivotValueProvider(io.viewserver.execution.nodes.IPivotValueProvider pivotValueProvider) {
        this.pivotValueProvider = pivotValueProvider;
    }

}
