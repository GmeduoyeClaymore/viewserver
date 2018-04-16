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

package io.viewserver.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Schema {
    private List<Column> columns;
    private List<String> keyColumns;

    public Schema() {
        columns = new ArrayList<>();
        keyColumns = new ArrayList<>();
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public Schema withColumns(List<Column> columns) {
        this.columns = columns;
        return this;
    }

    public List<String> getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(List<String> keyColumns) {
        this.keyColumns = keyColumns;
    }

    public Schema withKeyColumns(String... keyColumns) {
        this.keyColumns = Arrays.asList(keyColumns);
        return this;
    }

    public Column getColumn(String name) {
        int count = columns.size();
        for (int i = 0; i < count; i++) {
            Column column = columns.get(i);
            if (column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    public int getColumnId(String name) {
        int count = columns.size();
        for (int i = 0; i < count; i++) {
            if (columns.get(i).getName().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }
}
