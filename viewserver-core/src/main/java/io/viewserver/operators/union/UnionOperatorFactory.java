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

package io.viewserver.operators.union;

import io.viewserver.configurator.Configurator;
import io.viewserver.configurator.IInputConfigurator;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;
import io.viewserver.schema.ITableStorage;

/**
 * Created by nickc on 31/10/2014.
 */
public class UnionOperatorFactory extends ConfigurableOperatorFactoryBase<IUnionConfig> implements IInputConfigurator {
    private final ITableStorage.Factory tableStorageFactory;

    public UnionOperatorFactory(ITableStorage.Factory tableStorageFactory) {
        this.tableStorageFactory = tableStorageFactory;
    }

    @Override
    public String getOperatorType() {
        return "Union";
    }

    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return UnionOperator.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state, Object config) {
        return new UnionOperator(name, state.executionContext, state.catalog, tableStorageFactory.createStorage());
    }

    @Override
    public boolean configureInputs(IOperator operator, Object config) {
        UnionOperator unionOperator = (UnionOperator) operator;
        IUnionConfig unionConfig = (IUnionConfig) config;
        for (IUnionConfig.Input input : unionConfig.getInputs()) {
            unionOperator.getOrCreateInput(input.getName(), input.getSourceId());
        }
        return true;
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Class getProtoConfigDtoClass() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
