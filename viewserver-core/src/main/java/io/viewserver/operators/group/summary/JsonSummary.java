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

package io.viewserver.operators.group.summary;

import io.viewserver.operators.group.ISummary;
import io.viewserver.schema.ITableStorage;
import io.viewserver.schema.column.*;
import io.viewserver.schema.column.chunked.ChunkedColumnString;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by bemm on 27/10/2014.
 */
public class JsonSummary implements ISummary {
    private String name;
    private final String bucketColumn;
    private final String[] valueColumns;
    private ColumnHolderString summaryColumn;
    private ColumnHolder[] valueHolders;
    private ColumnHolder bucketHolder;
    private TIntObjectHashMap<TreeMap<Object, Integer>> rowMaps = new TIntObjectHashMap<>();
    private StringBuilder jsonBuilder = new StringBuilder();
    private ISummaryContext context;

    public JsonSummary(String name, String bucketColumn, String... valueColumns) {
        this.name = name;
        this.bucketColumn = bucketColumn;
        this.valueColumns = valueColumns;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean supportsPreviousValues() {
        return summaryColumn.supportsPreviousValues();
    }

    @Override
    public ColumnType getType() {
        return ColumnType.String;
    }

    @Override
    public void initialise(ISummaryContext context) {
        this.context = context;
        summaryColumn = (ColumnHolderString)ColumnHolderUtils.createColumnHolder(name, ColumnType.String);
        summaryColumn.setColumn(new JsonColumn(summaryColumn, context.getColumnWatcher(), context.getTableStorage(), 1024, 1024));
        context.setResultColumn(summaryColumn);
        bucketHolder = context.getInboundSchema().getColumnHolder(bucketColumn);
        valueHolders = new ColumnHolder[valueColumns.length];
        for (int i = 0; i < valueColumns.length; i++) {
            ColumnHolder columnHolder = context.getInboundSchema().getColumnHolder(valueColumns[i]);
            if (columnHolder == null) {
                throw new RuntimeException("No column '" + valueColumns[i] + "' exists");
            }
            valueHolders[i] = columnHolder;
        }
    }

    @Override
    public void onGroupAdd(int groupId) {
        rowMaps.put(groupId, new TreeMap<Object, Integer>());
    }

    @Override
    public void onGroupRemove(int groupId) {

    }

    @Override
    public void onGroupEnter(int groupId, int rowId) {
        TreeMap<Object, Integer> rowMap = rowMaps.get(groupId);
        Object bucketValue = ColumnHolderUtils.getValue(bucketHolder, rowId);
        rowMap.put(bucketValue, rowId);
        context.markDirty(groupId);
    }

    @Override
    public void onGroupLeave(int groupId, int rowId) {
        TreeMap<Object, Integer> rowMap = rowMaps.get(groupId);
        Object bucketValue = ColumnHolderUtils.getPreviousValue(bucketHolder, rowId);
        rowMap.remove(bucketValue);
        context.markDirty(groupId);
    }

    @Override
    public void onRowUpdate(int groupId, int rowId) {
        context.markDirty(groupId);
    }

    @Override
    public void onAfterCommit() {

    }

    @Override
    public boolean hasChanges(IRowFlags rowFlags) {
        if (rowFlags.isDirty(bucketHolder.getColumnId())) {
            return true;
        }
        for (ColumnHolder valueHolder : valueHolders) {
            if (rowFlags.isDirty(valueHolder.getColumnId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset() {
        ((IWritableColumnString)summaryColumn.getColumn()).resetAll();
        rowMaps.clear();
    }

    private String getJson(int row) {
        jsonBuilder.setLength(0);
        jsonBuilder.append('[');
        TreeMap<Object, Integer> rowMap = rowMaps.get(row);
        boolean first = true, quoteBucket = bucketHolder.getType().equals(ColumnType.String);
        for (Map.Entry<Object, Integer> entry : rowMap.entrySet()) {
            Object bucketValue = entry.getKey();
            int sourceRowId = entry.getValue();
            if (!first) {
                jsonBuilder.append(',');
            } else {
                first = false;
            }
            jsonBuilder.append('{');
            jsonBuilder.append("bucket:");
            if (quoteBucket) {
                jsonBuilder.append('"');
            }
            jsonBuilder.append(bucketValue);
            if (quoteBucket) {
                jsonBuilder.append('"');
            }
            for (ColumnHolder valueHolder : valueHolders) {
                jsonBuilder.append(',').append(valueHolder.getName()).append(':');
                Object value = ColumnHolderUtils.getValue(valueHolder, sourceRowId);
                boolean useQuotes = false;
                if ((useQuotes = valueHolder.getType().equals(ColumnType.String))) {
                    jsonBuilder.append('"');
                }
                jsonBuilder.append(value);
                if (useQuotes) {
                    jsonBuilder.append('"');
                }
            }
            jsonBuilder.append('}');
        }
        jsonBuilder.append(']');
        return jsonBuilder.toString();
    }

    private class JsonColumn extends ChunkedColumnString {
        private IColumnWatcher columnWatcher;

        public JsonColumn(ColumnHolder holder, IColumnWatcher columnWatcher, ITableStorage tableStorage, int capacity, int chunkSize) {
            super(holder, columnWatcher, tableStorage, capacity, chunkSize);
            this.columnWatcher = columnWatcher;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ColumnType getType() {
            return ColumnType.String;
        }

        @Override
        public String getString(int row) {
            String value = super.getString(row);
            if (value != null && !columnWatcher.isDirty(row, summaryColumn.getColumnId())) {
                return value;
            }

            String json = getJson(row);
            super.setString(row, json);
            return json;
        }
    }
}
