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

package io.viewserver.persistence;

import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nickc on 27/11/2014.
 */
public class HashMapPersistentStore implements IPersistentStore {
    private final Map<String, Table> map = new HashMap<>();

    @Override
    public void createTable(String name, Schema schema) {
        map.put(name, new Table(schema));
    }

    @Override
    public void addRow(String name, int rowId) {
        Table table = map.get(name);
        Map<String, Object> row = new HashMap<>();
        List<ColumnHolder> columnHolders = table.schema.getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder != null) {
                row.put(columnHolder.getName(), ColumnHolderUtils.getValue(columnHolder, rowId));
            }
        }
        table.rows.put(rowId, row);
    }

    @Override
    public void updateRow(String name, int rowId, IRowFlags rowFlags) {
        Table table = map.get(name);
        Map<String, Object> row = table.rows.get(rowId);
        List<ColumnHolder> columnHolders = table.schema.getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);
            if (columnHolder != null && rowFlags.isDirty(columnHolder.getColumnId())) {
                row.put(columnHolder.getName(), ColumnHolderUtils.getValue(columnHolder, rowId));
            }
        }
    }

    @Override
    public void removeRow(String name, int rowId) {
        Table table = map.get(name);
        table.rows.remove(rowId);
    }

    private class Table {
        private Schema schema;
        private TIntObjectHashMap<Map<String, Object>> rows = new TIntObjectHashMap<>();

        public Table(Schema schema) {
            this.schema = schema;
        }
    }
}
