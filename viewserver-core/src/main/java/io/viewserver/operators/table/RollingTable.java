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
import io.viewserver.collections.BoundedFifoBufferInt;
import io.viewserver.core.ExecutionContext;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;

/**
 * Created by nick on 01/10/15.
 */
public class RollingTable extends Table {
    private final BoundedFifoBufferInt rows;

    public RollingTable(String name, ExecutionContext executionContext, ICatalog catalog, Schema schema, ITableStorage storage, int size) {
        super(name, executionContext, catalog, schema, storage);
        rows = new BoundedFifoBufferInt(size);
    }

    @Override
    public int addRow(ITableRowUpdater updater) {
        if (rows.isFull()) {
            getOutput().handleRemove(rows.remove());
        }
        int rowId = super.addRow(updater);
        rows.add(rowId);
        return rowId;
    }

    @Override
    public void removeRow(int row) {
        throw new UnsupportedOperationException("RollingTable does not support explicit row removal");
    }
}
