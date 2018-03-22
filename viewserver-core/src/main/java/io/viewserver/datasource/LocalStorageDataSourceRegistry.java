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

import io.viewserver.catalog.ICatalog;
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.core.IJsonSerialiser;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;

/**
 * Created by nickc on 03/11/2014.
 */

public class LocalStorageDataSourceRegistry extends DataSourceRegistryBase<DataSource> {
    private final IDataLoader dataLoader;

    public LocalStorageDataSourceRegistry(ICatalog systemCatalog, IExecutionContext executionContext, IJsonSerialiser serialiser,
                                          ILocalStorageDataAdapterFactory localStorageDataAdapterFactory){
        super(systemCatalog, executionContext, serialiser, DataSource.class);

        dataLoader = localStorageDataAdapterFactory.getAdapter(TABLE_NAME, "datasources", 10);
    }

    public void loadDataSources() {
        LocalKeyedTableUpdater tableUpdater = new TableUpdater(executionContext, systemCatalog);
        final TableKeyDefinition tableKeyDefinition = getTableKeyDefinition();
        tableUpdater.setTableKeyDefinition(tableKeyDefinition);
        dataLoader.configure(tableUpdater, null, null,
                null, null, executionContext);
        dataLoader.setSchema(getOutput().getSchema());
        dataLoader.setTableKeyDefinition(tableKeyDefinition);
        dataLoader.createTable();
        dataLoader.load();
    }

    private class TableUpdater extends LocalKeyedTableUpdater {
        public TableUpdater(IExecutionContext executionContext, ICatalog catalog) {
            super(executionContext, catalog);
            table = LocalStorageDataSourceRegistry.this;
        }

        @Override
        public ITable createTable(String name, Schema schema) {
            return LocalStorageDataSourceRegistry.this;
        }
    }
}
