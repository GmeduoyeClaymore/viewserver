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
import io.viewserver.core.NullableBool;
import io.viewserver.datasource.IWritableDataAdapter;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.IRowFlags;

import java.util.BitSet;

/**
 * Created by nick on 19/02/2015.
 */
public class PersistentKeyedTable extends KeyedTable {
    private IWritableDataAdapter dataAdapter;
    private boolean isLoadingData;
    private final DirtyTrackingTableRow dirtyTrackingTableRow;

    public PersistentKeyedTable(String name, IExecutionContext executionContext, ICatalog catalog, Schema schema,
                                ITableStorage storage, TableKeyDefinition tableKeyDefinition, IWritableDataAdapter dataAdapter) {
        super(name, executionContext, catalog, schema, storage, tableKeyDefinition);
        this.dataAdapter = dataAdapter;
        dirtyTrackingTableRow = new DirtyTrackingTableRow();

        // don't do the initial reset on a persistent table - should be able to remove this when data adapters etc
        // are sorted out properly
        isSchemaResetRequested = isDataResetRequested = false;
    }

    public boolean isLoadingData() {
        return isLoadingData;
    }

    public void setLoadingData(boolean isLoadingData) {
        this.isLoadingData = isLoadingData;
    }

    @Override
    protected void onDataClear() {
        dataAdapter.clearData();

        super.onDataClear();
    }

    @Override
    public int addRow(ITableRowUpdater updater) {
        int rowId = super.addRow(updater);
        if (!isLoadingData) {
            tableRow.setRowId(rowId);
            dataAdapter.insertRecord(tableRow);
        }
        return rowId;
    }

    @Override
    public void updateRow(int row, ITableRowUpdater updater) {
        if (!isLoadingData) {
            super.updateRow(row, new DelegatingTableRowUpdater(updater) {
                @Override
                public void setValues(ITableRow tableRow) {
                    dirtyTrackingTableRow.clear();
                    super.setValues(dirtyTrackingTableRow);
                }
            });
            tableRow.setRowId(row);
            dataAdapter.updateRecord(tableRow, dirtyTrackingTableRow);
        } else {
            super.updateRow(row, updater);
        }
    }

    @Override
    public void removeRow(int rowId) {
        super.removeRow(rowId);
        tableRow.setRowId(rowId);
        dataAdapter.deleteRecord(tableRow);
    }

    private class DirtyTrackingTableRow implements ITableRow, IRowFlags {
        private BitSet dirtyFlags = new BitSet();

        @Override
        public int getRowId() {
            return tableRow.getRowId();
        }

        @Override
        public boolean getBool(String name) {
            return tableRow.getBool(name);
        }

        @Override
        public void setBool(String name, boolean value) {
            markDirty(name);
            tableRow.setBool(name, value);
        }

        @Override
        public NullableBool getNullableBool(String name) {
            return tableRow.getNullableBool(name);
        }

        @Override
        public void setNullableBool(String name, NullableBool value) {
            markDirty(name);
            tableRow.setNullableBool(name, value);
        }

        @Override
        public byte getByte(String name) {
            return tableRow.getByte(name);
        }

        @Override
        public void setByte(String name, byte value) {
            markDirty(name);
            tableRow.setByte(name, value);
        }

        @Override
        public short getShort(String name) {
            return tableRow.getShort(name);
        }

        @Override
        public void setShort(String name, short value) {
            markDirty(name);
            tableRow.setShort(name, value);
        }

        @Override
        public int getInt(String name) {
            return tableRow.getInt(name);
        }

        @Override
        public void setInt(String name, int value) {
            markDirty(name);
            tableRow.setInt(name, value);
        }

        @Override
        public long getLong(String name) {
            return tableRow.getLong(name);
        }

        @Override
        public void setLong(String name, long value) {
            markDirty(name);
            tableRow.setLong(name, value);
        }

        @Override
        public float getFloat(String name) {
            return tableRow.getFloat(name);
        }

        @Override
        public void setFloat(String name, float value) {
            markDirty(name);
            tableRow.setFloat(name, value);
        }

        @Override
        public double getDouble(String name) {
            return tableRow.getDouble(name);
        }

        @Override
        public void setDouble(String name, double value) {
            markDirty(name);
            tableRow.setDouble(name, value);
        }

        @Override
        public String getString(String name) {
            return tableRow.getString(name);
        }

        @Override
        public void setString(String name, String value) {
            markDirty(name);
            tableRow.setString(name, value);
        }

        @Override
        public Object getValue(String name) {
            return tableRow.getValue(name);
        }

        @Override
        public boolean isDirty(int columnId) {
            return false;
        }

        public void clear() {
            dirtyFlags.clear();
        }

        private void markDirty(String name) {
            int columnId = getOutput().getSchema().getColumnHolder(name).getColumnId();
            dirtyFlags.set(columnId);
        }
    }
}
