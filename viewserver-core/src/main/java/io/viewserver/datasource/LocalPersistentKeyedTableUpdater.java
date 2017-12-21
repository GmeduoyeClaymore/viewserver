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
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.PersistentKeyedTable;
import io.viewserver.operators.table.Table;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

/**
 * Created by nick on 19/02/2015.
 */
public class LocalPersistentKeyedTableUpdater extends LocalKeyedTableUpdater {
    private IWritableDataAdapter writableDataAdapter;

    public LocalPersistentKeyedTableUpdater(IExecutionContext executionContext, ICatalog catalog, IWritableDataAdapter writableDataAdapter) {
        super(executionContext, catalog);
        this.writableDataAdapter = writableDataAdapter;
    }

    @Override
    public ITable createTable(String name, Schema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("Schema must not be null");
        }
        table = new PersistentKeyedTable(name, executionContext, catalog, schema, new ChunkedColumnStorage(1024),
                tableKeyDefinition, writableDataAdapter);
        ((Table)table).initialise(8);
        return table;
    }

    @Override
    public void addRow(ITableRowUpdater rowUpdater) {
        ((PersistentKeyedTable) table).setLoadingData(true);
        super.addRow(rowUpdater);
        ((PersistentKeyedTable) table).setLoadingData(false);
    }
}
