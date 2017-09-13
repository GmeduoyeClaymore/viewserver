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

package io.viewserver.datasource;

import io.viewserver.core.ExecutionContext;
import io.viewserver.expression.IExpressionParser;
import io.viewserver.expression.function.FunctionRegistry;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.TableKeyDefinition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Created by nick on 16/02/2015.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY)
public interface IDataLoader {
    String getName();

    ITable getTable();

    IDataAdapter getDataAdapter();

    @JsonIgnore
    SettableFuture<Boolean> getLoadDataFuture();

    void configure(ITableUpdater tableUpdater, DimensionMapper dimensionMapper, DataSource dataSource, FunctionRegistry functionRegistry, IExpressionParser expressionParser, ExecutionContext executionContext);

    void createTable();

    ListenableFuture load();

    void setTableKeyDefinition(TableKeyDefinition tableKeyDefinition);

    void setSchema(io.viewserver.schema.Schema schema);
}
