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

package io.viewserver.schema.column;

/**
 * Created by nickc on 23/09/2014.
 */
public abstract class ColumnHolder implements IColumn {
    private IColumn column;
    private String name;
    private int columnId;
    private IRowMapper rowMapper;
    private ColumnMetadata metadata;

    protected ColumnHolder(String name) {
        this.name = name;
    }

    public ColumnHolder(String name, IRowMapper rowMapper) {
        this.name = name;
        this.rowMapper = rowMapper;
    }

    protected ColumnHolder(IColumn column, IRowMapper rowMapper) {
        this.column = column;
        this.rowMapper = rowMapper;
        this.name = column.getName();
    }

    public IColumn getColumn() {
        return column;
    }

    public void setColumn(IColumn column) {
        if (this.column != null) {
            throw new RuntimeException("Holder's column is already set!");
        }
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public abstract ColumnType getType();

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public int getColumnId() {
        return columnId;
    }

    public int getSourceRow(int row) {
        return rowMapper != null ? rowMapper.getSourceRow(row) : row;
    }

    public IRowMapper getRowMapper() {
        return rowMapper;
    }

    public ColumnMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ColumnMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', columnId=%d}", getClass().getSimpleName(), name, columnId);
    }
}
