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

package io.viewserver.operators.calccol;

import io.viewserver.configurator.Configurator;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.ConfigurableOperatorFactoryBase;
import io.viewserver.operators.IOperator;
import io.viewserver.schema.ITableStorage;

/**
 * Created by nickc on 31/10/2014.
 */
public class CalcColOperatorFactory extends ConfigurableOperatorFactoryBase<ICalcColConfig> {
    private final FunctionRegistry functionRegistry;
    private final ITableStorage.Factory tableStorageFactory;

    public CalcColOperatorFactory(FunctionRegistry functionRegistry, ITableStorage.Factory tableStorageFactory) {
        this.functionRegistry = functionRegistry;
        this.tableStorageFactory = tableStorageFactory;
    }

    @Override
    public String getOperatorType() {
        return "CalcCol";
    }

    @Override
    public Class<? extends IOperator> getOperatorClass() {
        return CalcColOperator.class;
    }

    @Override
    public IOperator createOperator(String name, Configurator.ConfiguratorState state) {
        return new CalcColOperator(name, state.executionContext, state.catalog, tableStorageFactory.createStorage(), state.executionContext.getExpressionParser());
    }

    @Override
    public Class getProtoConfigDtoClass() {
        return io.viewserver.messages.config.ICalcColConfig.class;
    }

    @Override
    public Object getProtoConfigWrapper(Object configDto) {
        return new ProtoCalcColConfig((io.viewserver.messages.config.ICalcColConfig) configDto);
    }
}
