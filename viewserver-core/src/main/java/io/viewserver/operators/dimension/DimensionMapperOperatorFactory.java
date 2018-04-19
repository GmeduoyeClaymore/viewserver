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

package io.viewserver.operators.dimension;

import io.viewserver.configurator.Configurator;
import io.viewserver.datasource.DimensionMapper;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.spread.SpreadOperator;
import io.viewserver.schema.ITableStorage;

/**
 * Created by nickc on 31/10/2014.
 */
public class DimensionMapperOperatorFactory extends ConfigurableOperatorFactoryBase<IDimensionMapConfig> {

    private DimensionMapper mapper;
    private ITableStorage.Factory tableStorageFactory;

    public DimensionMapperOperatorFactory(DimensionMapper mapper, ITableStorage.Factory tableStorageFactory) {
        this.mapper = mapper;
        this.tableStorageFactory = tableStorageFactory;
    }

    @Override
    public String getOperatorType() {
        return "DimensionMapper";
    }

    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return SpreadOperator.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state, Object config) {
        return new DimensionMapperOperator(name, state.executionContext, tableStorageFactory.createStorage(), state.catalog, mapper);
    }

    @Override
    public Class getProtoConfigDtoClass() {
        return io.viewserver.messages.config.IDimensionMapConfig.class;
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        return new ProtoDimensionMapConfig((io.viewserver.messages.config.IDimensionMapConfig) configDto);
    }
}
