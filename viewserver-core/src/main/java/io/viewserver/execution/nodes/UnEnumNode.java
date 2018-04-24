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

import io.viewserver.datasource.IDataSource;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.unenum.IUnEnumConfig;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by bemm on 03/11/2014.
 */
public class UnEnumNode extends GraphNodeBase<UnEnumNode> {
    private IDataSource dataSource;
    private List<String> dimensions = new ArrayList<>();

    public UnEnumNode(String name, IDataSource dataSource) {
        super(name, "UnEnum");
        this.dataSource = dataSource;
    }

    public UnEnumNode withDimensions(String... dimensions) {
        this.dimensions.addAll(Arrays.asList(dimensions));
        return this;
    }

    public UnEnumNode withDimensions(Collection<String> dimensions) {
        this.dimensions.addAll(dimensions);
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new IUnEnumConfig() {
            @Override
            public IDataSource getDataSource() {
                return dataSource;
            }

            @Override
            public List<String> getDimensions() {
                return dimensions;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        return null;
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        if(dataSource == null){
            return null;
        }
        return String.format("unenum:%s:%s", dataSource.getName(), StringUtils.join(dimensions, ','));
    }

    @Override
    public String toString() {
        return "UnEnumNode{" +
                "dimensions=" + dimensions +
                '}';
    }
}
