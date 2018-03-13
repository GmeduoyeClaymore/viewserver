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
import io.viewserver.operators.table.*;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

/**
 * Created by nick on 17/02/2015.
 */
public class LocalKeyedTableUpdater extends LocalTableUpdater implements IKeyedTableUpdater {
    protected TableKeyDefinition tableKeyDefinition;

    public LocalKeyedTableUpdater(IExecutionContext executionContext, ICatalog catalog) {
        super(executionContext, catalog);
    }

    @Override
    public TableKeyDefinition getTableKeyDefinition() {
        return tableKeyDefinition;
    }

    @Override
    public void setTableKeyDefinition(TableKeyDefinition tableKeyDefinition) {
        this.tableKeyDefinition = tableKeyDefinition;
    }

    @Override
    public ITable createTable(String name, Schema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("Schema must not be null");
        }
        table = new KeyedTable(name, executionContext, catalog, schema, new ChunkedColumnStorage(1024), tableKeyDefinition);
        ((Table)table).initialise(8);
        return table;
    }

    @Override
    public void updateRow(ITableRowUpdater rowUpdater) {
        ((KeyedTable)table).updateRow(rowUpdater);
    }

    @Override
    public int addOrUpdateRow(ITableRowUpdater rowUpdater) {
        return ((KeyedTable)table).addOrUpdateRow(rowUpdater);
    }
}