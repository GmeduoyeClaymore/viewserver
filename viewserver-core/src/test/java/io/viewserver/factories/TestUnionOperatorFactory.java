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
import io.viewserver.core.ExecutionContext;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.union.UnionOperator;
import io.viewserver.schema.ITableStorage;

import java.util.Map;

/**
 * Created by bemm on 01/12/2014.
 */
public class TestUnionOperatorFactory implements ITestOperatorFactory{
    private ExecutionContext executionContext;
    private ICatalog catalog;
    private ITableStorage tableStorage;

    public TestUnionOperatorFactory(ExecutionContext executionContext, ICatalog catalog, ITableStorage tableStorage) {
        this.executionContext = executionContext;
        this.catalog = catalog;
        this.tableStorage = tableStorage;
    }

    @Override
    public String getOperatorType() {
        return "union";
    }

    @Override
    public IOperator create(String operatorName, Map<String, Object> context) {
        UnionOperator unionOperator = new UnionOperator(operatorName, executionContext, catalog,tableStorage);
        configure(operatorName,context);
        return unionOperator;
    }

    @Override
    public void configure(String operatorName, Map<String, Object> config){

    }


}
