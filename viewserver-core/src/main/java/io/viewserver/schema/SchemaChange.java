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

import io.viewserver.schema.column.ColumnHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nickc on 20/10/2014.
 */
public class SchemaChange {
    private List<ColumnHolder> addedColumns = new ArrayList<>();
    private List<ColumnHolder> removedColumns = new ArrayList<>();

    protected void addColumn(ColumnHolder columnHolder) {
        addedColumns.add(columnHolder);
    }

    protected void removeColumn(ColumnHolder columnHolder) {
        removedColumns.add(columnHolder);
    }

    public List<ColumnHolder> getAddedColumns() {
        return addedColumns;
    }

    public List<ColumnHolder> getRemovedColumns() {
        return removedColumns;
    }

    public boolean hasChanges() {
        return !addedColumns.isEmpty() || !removedColumns.isEmpty();
    }

    public void clearChanges() {
        addedColumns.clear();
        removedColumns.clear();
    }
}
