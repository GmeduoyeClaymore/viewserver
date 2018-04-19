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

import io.viewserver.configurator.Configurator;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;

/**
 * Created by nick on 10/11/15.
 */
public class TablePartitionFactory extends ConfigurableOperatorFactoryBase<ITablePartitionConfig> {
    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return TablePartitionOperator.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state, Object config) {
        ITablePartitionConfig cfg = (ITablePartitionConfig) state.operatorSpec.getConfig();
        return new TablePartitionOperator(name, state.executionContext, state.catalog, cfg.getPartitionColumnName(),
                cfg.getPartitionValue());
    }

    @Override
    public Class getProtoConfigDtoClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        throw new UnsupportedOperationException();
    }
}
