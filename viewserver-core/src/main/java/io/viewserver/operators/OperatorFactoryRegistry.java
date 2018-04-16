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

package io.viewserver.operators;

import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.calccol.CalcColOperatorFactory;
import io.viewserver.operators.filter.FilterOperatorFactory;
import io.viewserver.operators.group.GroupByOperatorFactory;
import io.viewserver.operators.group.summary.SummaryRegistry;
import io.viewserver.operators.index.IndexOperatorFactory;
import io.viewserver.operators.join.JoinOperatorFactory;
import io.viewserver.operators.projection.ProjectionOperatorFactory;
import io.viewserver.operators.sort.SortOperatorFactory;
import io.viewserver.operators.spread.ISpreadFunctionRegistry;
import io.viewserver.operators.spread.SpreadOperatorFactory;
import io.viewserver.operators.table.TablePartitionFactory;
import io.viewserver.operators.table.UserSessionPartitionerFactory;
import io.viewserver.operators.transpose.TransposeOperatorFactory;
import io.viewserver.operators.union.UnionOperatorFactory;
import io.viewserver.schema.ITableStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickc on 31/10/2014.
 */
public class OperatorFactoryRegistry {
    private final Map<String, IOperatorFactory> factories = new HashMap<>();

    public OperatorFactoryRegistry(ISpreadFunctionRegistry spreadColumnRegistry, FunctionRegistry functionRegistry, ITableStorage.Factory tableStorageFactory,
                                   SummaryRegistry summaryRegistry) {
        register(new CalcColOperatorFactory(tableStorageFactory));
        register(new SpreadOperatorFactory(spreadColumnRegistry, tableStorageFactory));
        register(new FilterOperatorFactory(functionRegistry));
        register(new GroupByOperatorFactory(tableStorageFactory, summaryRegistry));
        register(new JoinOperatorFactory());
        register(new SortOperatorFactory(tableStorageFactory));
        register(new TransposeOperatorFactory(tableStorageFactory));
        register(new UnionOperatorFactory(tableStorageFactory));
        register(new IndexOperatorFactory());
        register(new ProjectionOperatorFactory());
        register(new TablePartitionFactory());
        register(new UserSessionPartitionerFactory());
    }

    public void register(IOperatorFactory factory) {
        factories.put(factory.getOperatorType(), factory);
    }

    public IOperatorFactory get(String operatorType) {
        return factories.get(operatorType);
    }
}
