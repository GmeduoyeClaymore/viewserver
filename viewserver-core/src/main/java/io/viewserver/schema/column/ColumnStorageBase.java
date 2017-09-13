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

package io.viewserver.schema.column;

import io.viewserver.core.IGrowthPolicy;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;

import java.util.List;

/**
 * Created by nickc on 26/09/2014.
 */
public abstract class ColumnStorageBase implements ITableStorage {
    private final IColumnFactory columnFactory;
    private IGrowthPolicy growthPolicy = IGrowthPolicy.MinimumGrowth;
    private int capacity;
    private IColumnWatcher columnWatcher;

    protected ColumnStorageBase(IColumnFactory columnFactory) {
        this.columnFactory = columnFactory;
        columnFactory.setTableStorage(this);
    }

    @Override
    public void initialise(int capacity, Schema schema, IColumnWatcher columnWatcher) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }
        if (schema == null) {
            throw new IllegalArgumentException("Schema cannot be null");
        }

        this.columnWatcher = columnWatcher;
        this.capacity = capacity;
        columnFactory.initialise(capacity);

        List<ColumnHolder> columnHolders = schema.getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder == null) {
                continue;
            }

            initialiseColumn(columnHolder);
        }
    }

    @Override
    public void initialiseColumn(ColumnHolder columnHolder) {
        if (columnHolder.getColumn() == null) {
            columnHolder.setColumn(columnFactory.createColumn(columnHolder, columnWatcher));
        }
    }

    @Override
    public void ensureCapacity(int requiredCapacity, Schema schema) {
        if (requiredCapacity >= capacity) {
            capacity = growthPolicy.getNewSize(capacity, requiredCapacity);
            List<ColumnHolder> columnHolders = schema.getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null || !(columnHolder instanceof IGrowableColumn) || ((IGrowableColumn) columnHolder).getTableStorage() != this) {
                    continue;
                }
                ((IGrowableColumn)columnHolder.getColumn()).ensureCapacity(capacity);
            }
            columnFactory.initialise(capacity);
        }
    }
}
