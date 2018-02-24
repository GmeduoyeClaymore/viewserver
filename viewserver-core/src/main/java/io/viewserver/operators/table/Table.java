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

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.changequeue.ChangeQueue;
import io.viewserver.core.IExecutionContext;
import io.viewserver.operators.*;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import gnu.trove.list.array.TIntArrayList;

public class Table extends InputOperatorBase implements IInputOperator, ITable {
    private Output output;
    private int nextRowId = 0;
    private TIntArrayList freeRowIds = new TIntArrayList(8, -1);
    private boolean freeRowIdsSorted = true;
    protected boolean initialised;
    private ITableStorage storage;
    protected TableRow tableRow;

    public Table(String name, IExecutionContext executionContext, ICatalog catalog, Schema schema, ITableStorage storage) {
        super(name, executionContext, catalog);
        this.storage = storage;

        output = new Output(Constants.OUT, this);
        addOutput(output);

        if (schema != null) {
            setSchema(schema);
        }
    }

    public ITableStorage getStorage() {
        return storage;
    }

    public void setSchema(Schema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("Schema must not be null");
        }
        output.setSchema(schema);
        tableRow = new TableRow(0, schema);
    }

    public void initialise(int capacity) {
        if (initialised) {
            throw new RuntimeException("Table already initialised");
        }

        storage.initialise(capacity, output.schema, output.getCurrentChanges());

        initialised = true;
    }

    public IOutput getOutput() {
        return output;
    }

    @Override
    public int addRow(ITableRowUpdater updater) {
        int row = getFreeRowId();
        storage.ensureCapacity(row + 1, output.schema);
        tableRow.setRowId(row);
        updater.setValues(tableRow);
        output.handleAdd(row);
        return row;
    }

    private int getFreeRowId() {
        if (!freeRowIds.isEmpty()) {
            if (!freeRowIdsSorted) {
                freeRowIds.sort();
                freeRowIdsSorted = true;
            }
            return freeRowIds.removeAt(0);
        }
        return nextRowId++;
    }

    @Override
    public void updateRow(int row, ITableRowUpdater updater) {
        tableRow.setRowId(row);
        updater.setValues(tableRow);
        output.handleUpdate(row);

    }

    @Override
    public void removeRow(int row) {
        output.handleRemove(row);
    }

    public void enableReferenceCounting() {
        ((ChangeQueue)output.getCurrentChanges()).setReferenceCountingEnabled(true);
    }

    private class Output extends OutputBase {
        private Schema schema;

        private Output(String name, IOperator owner) {
            super(name, owner);
        }

        private void setSchema(Schema schema) {
            this.schema = schema;
            this.schema.setOwner(this);
        }

        @Override
        public Schema getSchema() {
            return schema;
        }

        @Override
        public void clearData() {
            super.clearData();
            // we don't reset nextRowId, because we could have pending row operations that have already been given row IDs
        }
    }
}
