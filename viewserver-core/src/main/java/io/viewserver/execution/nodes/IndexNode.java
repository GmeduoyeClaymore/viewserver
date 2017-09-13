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
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.index.IIndexConfig;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by paulg on 12/11/2014.
 */
public class IndexNode extends GraphNodeBase<IndexNode> {
    String[] indexedColumns;

    public IndexNode(){super();}

    public IndexNode(String name) {
        super(name, "Index");
    }

    public IndexNode withIndexedColumns(String... indexedColumns){
        this.indexedColumns = indexedColumns;
        return this;
    }

    public IndexNode withIndexedColumns(List<String> indexedColumns){
        this.indexedColumns = indexedColumns.toArray(new String[indexedColumns.size()]);
        return this;
    }

    @Override
    public Object getConfig(ParameterHelper parameterHelper) {
        return new IIndexConfig() {
            @Override
            public String[] getIndices() {
                return indexedColumns;
            }

            @Override
            public Output[] getOutputs() {
                return null;
            }
        };
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        return null;
    }

    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return String.format("index:%s", StringUtils.join(indexedColumns, ','));
    }

    public String[] getIndexedColumns() {
        return indexedColumns;
    }

    public void setIndexedColumns(String[] indexedColumns) {
        this.indexedColumns = indexedColumns;
    }
}
