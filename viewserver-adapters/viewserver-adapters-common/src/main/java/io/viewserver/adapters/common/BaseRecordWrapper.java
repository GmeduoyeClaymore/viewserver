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

package io.viewserver.adapters.common;

import io.viewserver.datasource.Column;
import io.viewserver.datasource.ColumnOption;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IRecord;

import java.util.List;


public abstract class BaseRecordWrapper implements IRecord {
    protected DataSource dataSource;

    protected BaseRecordWrapper() {
    }

    protected BaseRecordWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String[] getColumnNames() {
        List<Column> columns = dataSource.getSchema().getColumns();
        String[] columnNames = new String[columns.size()];
        int i = 0;
        for (Column column: columns) {
            columnNames[i++] = column.getName();
        }
        return columnNames;
    }

    protected Column getColumn(String columnName){
        return dataSource.getSchema().getColumn(columnName);
    }

    protected <T> T replaceNullValues(String columnName, Object value, Class<T> type){
        if(this.getColumn(columnName).hasOption(ColumnOption.ReplaceNullWithDefaultValue) && value == null){
            value = this.getColumn(columnName).getDefaultValue();
        }
        return type.cast(value);
    }
}
