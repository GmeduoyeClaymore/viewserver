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

package io.viewserver.operators.sort;

import io.viewserver.configurator.Configurator;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;
import io.viewserver.schema.ITableStorage;

/**
 * Created by nickc on 31/10/2014.
 */
public class SortOperatorFactory extends ConfigurableOperatorFactoryBase<ISortConfig> {
    private final ITableStorage.Factory tableStorageFactory;

    public SortOperatorFactory(ITableStorage.Factory tableStorageFactory) {
        this.tableStorageFactory = tableStorageFactory;
    }

    @Override
    public String getOperatorType() {
        return "Sort";
    }

    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return SortOperator.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state) {
        return new SortOperator(name, state.executionContext, state.catalog, tableStorageFactory.createStorage());
    }

    @Override
    public Class getProtoConfigDtoClass() {
        return io.viewserver.messages.config.ISortConfig.class;
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        return new ProtoSortConfig((io.viewserver.messages.config.ISortConfig) configDto);
    }
}
