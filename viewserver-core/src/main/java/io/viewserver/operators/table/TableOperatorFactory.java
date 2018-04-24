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

package io.viewserver.operators.table;

import io.viewserver.command.CommandResult;
import io.viewserver.configurator.Configurator;
import io.viewserver.datasource.SchemaConfig;
import io.viewserver.execution.nodes.TableNode;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.ColumnHolderUtils;

/**
 * Created by bemm on 31/10/2014.
 */
public class TableOperatorFactory extends ConfigurableOperatorFactoryBase<ISchemaConfig> {

    private final ITableStorage.Factory tableStorageFactory;

    public TableOperatorFactory(ITableStorage.Factory tableStorageFactory) {
        this.tableStorageFactory = tableStorageFactory;
    }

    @Override
    public String getOperatorType() {
        return "Table";
    }

    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return KeyedTable.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state, Object config) {
        ISchemaConfig schemaConfig = (ISchemaConfig) config;
        KeyedTable operator = new KeyedTable(name, state.executionContext, state.catalog, ColumnHolderUtils.getSchema(schemaConfig),tableStorageFactory.createStorage(), schemaConfig.getTableKeyDefinition());
        operator.initialise(8);
        return operator;
    }

    @Override
    public void configureOperator(IOperator operator, Object config, Configurator.ConfiguratorState state, CommandResult configureResult) {
    }

    @Override
    public Class getProtoConfigDtoClass() {
        return io.viewserver.messages.config.ISchemaConfig.class;
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        return new ProtoTableConfig((io.viewserver.messages.config.ISchemaConfig) configDto);
    }
}
