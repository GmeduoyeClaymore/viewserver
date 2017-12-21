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

package io.viewserver.factories;

import io.viewserver.catalog.ICatalog;
import io.viewserver.command.CommandResult;
import io.viewserver.configurator.Configurator;
import io.viewserver.configurator.IConfiguratorSpec;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.IConfigurableOperatorFactory;
import io.viewserver.operators.IOperator;

import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public abstract class TestOperatorFactory implements ITestOperatorFactory{

    private final String operatorType;
    private IConfigurableOperatorFactory configurableOperatorFactory;
    private IExecutionContext executionContext;
    private ICatalog catalog;

    public TestOperatorFactory(IConfigurableOperatorFactory configurableOperatorFactory1, IExecutionContext  executionContext, ICatalog catalog) {
        this.configurableOperatorFactory = configurableOperatorFactory1;
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.operatorType  = configurableOperatorFactory1.getOperatorType();
    }

    @Override
    public String getOperatorType() {
        return operatorType;
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        IConfiguratorSpec spec = createSpec(context);
        IOperator operator = configurableOperatorFactory.createOperator(operatorName,new Configurator.ConfiguratorState(spec,executionContext,catalog, new CommandResult()));
        configurableOperatorFactory.configureOperator(operator,spec.getOperators().get(0).getConfig(), null, new CommandResult());
        return operator;
    }

    protected  abstract IConfiguratorSpec createSpec(Map<String, Object> context);
}
