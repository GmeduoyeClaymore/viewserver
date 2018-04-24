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

package io.viewserver.operators.join;

import io.viewserver.configurator.Configurator;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;

/**
 * Created by bemm on 31/10/2014.
 */
public class JoinOperatorFactory extends ConfigurableOperatorFactoryBase<IJoinConfig> {
    @Override
    public String getOperatorType() {
        return "Join";
    }

    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return JoinOperator.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state, Object config) {
        return new JoinOperator(name, state.executionContext, state.catalog);
    }

    @Override
    public Class getProtoConfigDtoClass() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
