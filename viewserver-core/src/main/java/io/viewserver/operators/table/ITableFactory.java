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

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.schema.Schema;

/**
 * Created by bemm on 01/10/15.
 */
public interface ITableFactory<TTable extends ITable, TConfig> {
    TTable create(String name, IExecutionContext executionContext, ICatalog catalog, Schema schema, TConfig config);

    Class<TTable> getTableClass();

    Class getProtoConfigDtoClass();

    IRollingTableConfig getProtoConfigWrapper(Object configDto);
}
