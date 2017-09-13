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

package io.viewserver.r.client;

import io.viewserver.client.ClientSubscriptionBase;
import io.viewserver.client.ViewServerClient;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.deserialiser.DeserialiserOperator;
import io.viewserver.operators.sort.ColumnRadixSort;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderFloat;
import io.viewserver.schema.column.ColumnHolderInt;
import io.viewserver.schema.column.ColumnHolderLong;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 14/07/15.
 */
public class RClientSubscription extends ClientSubscriptionBase {
    private final ViewServerClient client;
    private int[] rowIds;
    private int currentRow = 0;

    public RClientSubscription(DeserialiserOperator deserialiserOperator, ViewServerClient client) {
        super(deserialiserOperator);
        this.client = client;
    }

    public Column[] getSchema() {
        Schema schema = deserialiserOperator.getOutput().getSchema();
        Column[] columns = new Column[schema.getWidth()-1];
        int i = 0;
        List<ColumnHolder> columnHolders = schema.getColumnHolders();
        int count = columnHolders.size();
        for (int j = 0; j < count; j++) {
            ColumnHolder columnHolder = columnHolders.get(j);
            if (columnHolder == null || "rank".equals(columnHolder.getName())) {
                continue;
            }
            columns[i] = new Column(columnHolder);
            i++;
        }

        return columns;
    }

    private String getRType(ColumnHolder columnHolder) {
        if (columnHolder.getMetadata() != null && columnHolder.getMetadata().getDataType() != null) {
            switch (columnHolder.getMetadata().getDataType()) {
                case Bool:
                case NullableBool: {
                    return "logical";
                }
                case Byte:
                case Short:
                case Int:
                case Long: {
                    return "integer";
                }
                case Float:
                case Double: {
                    return "double";
                }
                case String: {
                    return "character";
                }
                case DateTime: {
                    return "timestamp";
                }
                case Date: {
                    return "date";
                }
                default: {
                    throw new IllegalArgumentException("Unknown column type '" + columnHolder.getMetadata().getDataType() + "'");
                }
            }
        } else {
            switch (columnHolder.getType()) {
                case Bool:
                case NullableBool: {
                    return "logical";
                }
                case Byte:
                case Short:
                case Int:
                case Long: {
                    return "integer";
                }
                case Float:
                case Double: {
                    return "double";
                }
                case String: {
                    return "character";
                }
                default: {
                    throw new IllegalArgumentException("Unknown column type '" + columnHolder.getType() + "'");
                }
            }
        }
    }

    public Iterable<ColumnValues> getRows(int numRows) {
        Schema schema = deserialiserOperator.getOutput().getSchema();

        if (rowIds == null) {
            IRowSequence cursor = deserialiserOperator.getOutput().getAllRows();
            rowIds = new int[deserialiserOperator.getOutput().getRowCount()];
            for (int i = 0; cursor.moveNext(); i++) {
                rowIds[i] = cursor.getRowId();
            }
            rowIds = ColumnRadixSort.sort2(rowIds, (ColumnHolderInt) schema.getColumnHolder("rank"), false);
        }

        ArrayList<Integer> fetchRowIds = new ArrayList<>();
        for (; currentRow < rowIds.length && (numRows < 0 || fetchRowIds.size() < numRows); currentRow++) {
            fetchRowIds.add(rowIds[currentRow]);
        }
        int[] fetchRowIdArray = new int[fetchRowIds.size()];
        for (int i = 0; i < fetchRowIds.size(); i++) {
            fetchRowIdArray[i] = fetchRowIds.get(i);
        }

        ArrayList<ColumnValues> columnValues = new ArrayList<>();
        columnValues.add(new ColumnValues("rowId", fetchRowIdArray));
        List<ColumnHolder> columnHolders = schema.getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder == null || "rank".equals(columnHolder.getName())) {
                continue;
            }

            Object values;
            switch (columnHolder.getType()) {
                case Bool: {
                    throw new UnsupportedOperationException("bool");
                }
                case NullableBool: {
                    throw new UnsupportedOperationException("nullable bool");
                }
                case Byte: {
                    throw new UnsupportedOperationException("byte");
                }
                case Short: {
                    throw new UnsupportedOperationException("short");
                }
                case Int: {
                    int[] ints = new int[fetchRowIds.size()];
                    int rowIdCount = fetchRowIds.size();
                    for (int j = 0; j < rowIdCount; j++) {
                        ints[j] = ((ColumnHolderInt) columnHolder).getInt(fetchRowIds.get(j));
                    }
                    values = ints;
                    break;
                }
                case Long: {
                    long[] longs = new long[fetchRowIds.size()];
                    int rowIdCount = fetchRowIds.size();
                    for (int j = 0; j < rowIdCount; j++) {
                        longs[j] = ((ColumnHolderLong) columnHolder).getLong(fetchRowIds.get(j));
                    }
                    values = longs;
                    break;
                }
                case Float: {
                    float[] floats = new float[fetchRowIds.size()];
                    int rowIdCount = fetchRowIds.size();
                    for (int j = 0; j < rowIdCount; j++) {
                        floats[j] = ((ColumnHolderFloat) columnHolder).getFloat(fetchRowIds.get(j));
                    }
                    values = floats;
                    break;
                }
                case Double: {
                    throw new UnsupportedOperationException("double");
                }
                case String: {
                    throw new UnsupportedOperationException("string");
                }
                default: {
                    throw new RuntimeException("Unknown column type '" + columnHolder.getType() + "'");
                }
            }
            columnValues.add(new ColumnValues(columnHolder.getName(), values));
        }

        return columnValues;
    }

    public class Column {
        private ColumnHolder columnHolder;

        public Column(ColumnHolder columnHolder) {
            this.columnHolder = columnHolder;
        }

        public String getName() {
            return columnHolder.getName();
        }

        public String getRType() {
            return RClientSubscription.this.getRType(columnHolder);
        }

        public String getVSType() {
            return columnHolder.getMetadata() != null ? columnHolder.getMetadata().getDataType().toString() : columnHolder.getType().toString();
        }
    }

    public class ColumnValues {
        private String name;
        private Object values;

        public ColumnValues(String name, Object values) {
            this.name = name;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public Object getValues() {
            return values;
        }
    }
}
