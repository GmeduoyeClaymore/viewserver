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

package io.viewserver.operators.group;

import io.viewserver.configurator.Configurator;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.schema.ITableStorage;

/**
 * Created by bemm on 31/10/2014.
 */
public class GroupByOperatorFactory extends ConfigurableOperatorFactoryBase<IGroupByConfig> {
    private final ITableStorage.Factory tableStorageFactory;
    private SummaryRegistry summaryRegistry;

    public GroupByOperatorFactory(ITableStorage.Factory tableStorageFactory, SummaryRegistry summaryRegistry) {
        this.tableStorageFactory = tableStorageFactory;
        this.summaryRegistry = summaryRegistry;
    }

    @Override
    public String getOperatorType() {
        return "GroupBy";
    }

    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return GroupByOperator.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state, Object config) {
        return new GroupByOperator(name, state.executionContext, state.catalog, summaryRegistry, tableStorageFactory.createStorage());
    }

    @Override
    public Class getProtoConfigDtoClass() {
        return io.viewserver.messages.config.IGroupByConfig.class;
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        return new ProtoGroupByConfig((io.viewserver.messages.config.IGroupByConfig) configDto);
    }
}
