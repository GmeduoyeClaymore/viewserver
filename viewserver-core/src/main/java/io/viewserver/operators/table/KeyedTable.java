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
import io.viewserver.core.ExecutionContext;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.util.ViewServerException;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bemm on 23/09/2014.
 */

public class KeyedTable extends Table {

    protected TableKeyDefinition tableKeyDefinition;
    private TObjectIntHashMap<Object> keys = new TObjectIntHashMap<>(8, 0.75f, -1);

    public KeyedTable(String name, IExecutionContext executionContext, ICatalog catalog, Schema schema, ITableStorage storage, TableKeyDefinition tableKeyDefinition) {
        super(name, executionContext, catalog, schema, storage);

        if (tableKeyDefinition == null) {
            throw new RuntimeException(String.format("Unable to create a keyed table \"%s\" with a null table key definition", name));
        }
        this.isDataResetRequested = false;
        this.tableKeyDefinition = tableKeyDefinition;
    }

    @Override
    public void initialise(int capacity) {
        if (initialised) {
            throw new RuntimeException("Table already initialised");
        }

        for (String keyColumn : tableKeyDefinition.getKeys()) {
            ColumnHolder keyHolder = this.getOutput().getSchema().getColumnHolder(keyColumn);

            // add the key column for the caller if it wasn't declared explicitly in the schema
            if (keyHolder == null) {
                throw new RuntimeException(String.format("The key column %s was not defined in the schema, it should be defined", keyColumn));
            }
        }

        super.initialise(capacity);
        register();
    }

    @Override
    public int addRow(ITableRowUpdater updater) {
        return super.addRow(new DelegatingTableRowUpdater(updater) {
            @Override
            public void setValues(ITableRow row) {
                super.setValues(row);

                TableKey tableKey = getTableKey(row);

                storeKeyForRow(tableKey, row.getRowId());
            }
        });
    }

    // TODO: set key column values in here
    public int addRow(TableKey tableKey, ITableRowUpdater updater) {
        int rowId = getRow(tableKey);
        if (rowId != -1) {
            throw new ViewServerException("A row already exists with that key");
        }

        rowId = super.addRow(updater);
        storeKeyForRow(tableKey, rowId);
        return rowId;
    }

    @Override
    public void updateRow(int row, ITableRowUpdater updater) {
        super.updateRow(row, new DelegatingTableRowUpdater(updater) {
            @Override
            public void setValues(ITableRow tableRow) {
                TableKey oldKey = KeyedTable.this.getTableKey(tableRow);

                super.setValues(tableRow);

                TableKey newKey = KeyedTable.this.getTableKey(tableRow);
                if (!newKey.equals(oldKey)) {
                    KeyedTable.this.removeKey(oldKey);
                    KeyedTable.this.storeKeyForRow(newKey, row);
                }
            }
        });
    }

    public void updateRow(TableKey tableKey, ITableRowUpdater updater) {
        ExecutionContext.AssertUpdateThread();
        Object keyValue = tableKeyDefinition.getValue(tableKey);
        int rowId = keys.get(keyValue);
        if (rowId == -1) {
            throw new ViewServerException("A row does not exist for key " + keyValue);
        }
        super.updateRow(rowId, updater);
    }

    public void updateRow(ITableRowUpdater updater) {
        updateRow(getTableKey(updater), updater);
    }

    public int addOrUpdateRow(ITableRowUpdater updater) {
        ExecutionContext.AssertUpdateThread();
        TableKey tableKey = getTableKey(updater);
        int rowId = getRow(tableKey);
        if (rowId != -1) {
            updateRow(tableKey, updater);
            return rowId;
        } else {
            return addRow(updater);
        }
    }

    private TableKey getTableKey(ITableRowUpdater updater) {
        List<String> keys = tableKeyDefinition.getKeys();
        int count = keys.size();
        Object[] keyValues = new Object[count];
        for (int i = 0; i < count; i++) {
            keyValues[i] = updater.getValue(keys.get(i));
        }
        return new TableKey(keyValues);
    }

    @Override
    public void removeRow(int rowId) {
        tableRow.setRowId(rowId);
        TableKey tableKey = getTableKey(tableRow);
        removeRow(tableKey);
    }

    public void removeRow(TableKey tableKey) {
        int rowId = removeKey(tableKey);
        super.removeRow(rowId);
    }

    public int getRow(TableKey tableKey){
        Object keyValue = tableKeyDefinition.getValue(tableKey);
        return keys.get(keyValue);
    }

    protected TableKey getTableKey(ITableRow row) {
        List<Object> keyValues = new ArrayList<>();
        List<String> keys = tableKeyDefinition.getKeys();
        int count = keys.size();
        for (int i = 0; i < count; i++) {
            keyValues.add(row.getValue(keys.get(i)));
        }

        return new TableKey(keyValues.toArray());
    }

    public TableKey getTableKey(IRecord record) {
        List<Object> keyValues = new ArrayList<>();
        List<String> keys = tableKeyDefinition.getKeys();
        int count = keys.size();
        for (int i = 0; i < count; i++) {
            keyValues.add(record.getValue(keys.get(i)));
        }

        return new TableKey(keyValues.toArray());
    }

    private void storeKeyForRow(TableKey tableKey, int rowId) {
        Object keyValue = tableKeyDefinition.getValue(tableKey);
        if(tableKey.size() != tableKeyDefinition.size()){
            throw new RuntimeException(String.format("There must be exactly %d values in the table key \"%s\"", tableKeyDefinition.size(),tableKey));
        }
        keys.put(keyValue, rowId);
    }

    private int removeKey(TableKey tableKey) {
        Object keyValue = tableKeyDefinition.getValue(tableKey);
        return keys.remove(keyValue);
    }

    public TableKeyDefinition getTableKeyDefinition() {
        return tableKeyDefinition;
    }
}
