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

import io.viewserver.datasource.Column;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.execution.ParameterHelper;
import io.viewserver.messages.MessagePool;
import io.viewserver.messages.config.IOperatorConfig;
import io.viewserver.operators.table.ISchemaConfig;

import java.util.ArrayList;
import java.util.List;


public class TableNode extends GraphNodeBase<TableNode> {
    private ISchemaConfig config;

    public TableNode(){
        super();
    }

    public TableNode(String name) {
        super(name, "Table");
    }

    public TableNode withSchemaConfig(ISchemaConfig config) {
        this.config = config;
        return this;
    }

    public ISchemaConfig getConfig() {
        return config;
    }

    @Override
    public Object getConfig(final ParameterHelper parameterHelper) {
        return new ISchemaConfig() {

            @Override
            public List<Column> getColumns() {
                return parameteriseCols(config.getColumns(),parameterHelper);
            }

            @Override
            public List<String> getKeyColumns() {
                return parameterise(config.getKeyColumns(),parameterHelper);
            }
        };
    }

    private List<Column> parameteriseCols(List<Column> columns, ParameterHelper parameterHelper) {
        List<Column> result = new ArrayList<>();
        for(Column col: columns){
            result.add(new Column(parameterHelper.substituteParameterValues(col.getName()), col.getType()));
        }
        return result;
    }

    private List<String> parameterise(List<String> columns,ParameterHelper parameterHelper) {
        List<String> result = new ArrayList<>();
        for(String col: columns){
            result.add(parameterHelper.substituteParameterValues(col));
        }
        return result;
    }

    private SchemaConfig parameterise(ISchemaConfig config, ParameterHelper helper) {
        return new SchemaConfig().setKeyColumns(parameterise(config.getKeyColumns(),helper)).setColumns(parameteriseCols(config.getColumns(),helper));
    }

    @Override
    protected IOperatorConfig getConfigDto(ParameterHelper parameterHelper) {
        return MessagePool.getInstance().get(io.viewserver.messages.config.ISchemaConfig.class)
                .setKeyColumns(parameterise(config.getKeyColumns(),parameterHelper))
                .setColumns(parameterise(config.getKeyColumns(),parameterHelper));
    }


    @Override
    protected String getConfigForOperatorName(ParameterHelper parameterHelper) {
        return String.format("table:%s", parameterise(this.config, parameterHelper));
    }


    @Override
    public String toString() {
        return "TableNode{" +
                "config=" + config +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}
