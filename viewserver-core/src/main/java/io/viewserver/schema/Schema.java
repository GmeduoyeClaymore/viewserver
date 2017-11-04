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

package io.viewserver.schema;

import io.viewserver.datasource.Column;
import io.viewserver.operators.IOutput;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnType;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by nickc on 23/09/2014.
 */
public class Schema {
    private final List<ColumnHolder> columnHolders = new ArrayList<ColumnHolder>();
    private List<ColumnHolder> columnHoldersCopy;
    private final TObjectIntHashMap<String> columnNameToIdMap;
    private final SchemaChange change;
    private int width = 0;
    private IOutput owner;

    public Schema() {
        columnNameToIdMap = new TObjectIntHashMap<>(8, 0.75f, -1);
//        columnNameToIdMap.setUp(8);
        change = new SchemaChange();
    }

    public IOutput getOwner() {
        return owner;
    }

    public void setOwner(IOutput owner) {
        this.owner = owner;
    }

    public List<ColumnHolder> getColumnHolders() {
        if (columnHoldersCopy == null) {
            columnHoldersCopy = Collections.unmodifiableList(columnHolders);
        }
        return columnHoldersCopy;
    }

    public ColumnHolder getColumnHolder(int columnId) {
        return columnId >= 0 && columnId < columnHolders.size() ? columnHolders.get(columnId) : null;
    }

    public ColumnHolder getColumnHolder(String name) {
        int columnId = columnNameToIdMap.get(name);
        if (columnId != columnNameToIdMap.getNoEntryValue()) {
            return getColumnHolder(columnId);
        }
        return null;
    }

    public int addColumn(ColumnHolder columnHolder) {
        String name = columnHolder.getName();
        if (columnNameToIdMap.containsKey(name)) {
            throw new RuntimeException(String.format("Column %s already exists in the schema", name));
        }
        //TODO: reuse removed columns
        int columnId = columnHolders.size();
        columnHolder.setColumnId(columnId);
        columnNameToIdMap.put(name, columnId);
        columnHoldersCopy = null;
        columnHolders.add(columnId, columnHolder);
        width++;
        change.addColumn(columnHolder);
        return columnId;
    }

    public int addColumn(String name, ColumnType columnType) {
        return addColumn(ColumnHolderUtils.createColumnHolder(name, columnType));
    }

    public void removeColumn(int columnId) {
        ColumnHolder columnHolder = columnHolders.set(columnId, null);
        columnHoldersCopy = null;
        if (columnHolder == null) {
            throw new IllegalArgumentException(String.format("Schema has no column ID %d", columnId));
        }
        columnNameToIdMap.remove(columnHolder.getName());
        width--;
        change.removeColumn(columnHolder);
    }

    public int getWidth() {
        return width;
    }

    public SchemaChange getChange() {
        return change;
    }

    public void clear() {
        columnHolders.clear();
        columnHoldersCopy = null;
        columnNameToIdMap.clear();
        change.clearChanges();
        width = 0;
    }

    public Schema withColumns(List<Column> columns) {
        for(Column col : columns){
            this.addColumn(ColumnHolderUtils.createColumnHolder(col.getName(),col.getType().getColumnType()));
        }
        return this;
    }
}

