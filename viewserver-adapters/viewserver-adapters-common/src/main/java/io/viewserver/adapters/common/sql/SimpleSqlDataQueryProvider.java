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

package io.viewserver.adapters.common.sql;

import io.viewserver.adapters.common.IWritableDataQueryProvider;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;

import java.util.List;

public class SimpleSqlDataQueryProvider implements IWritableDataQueryProvider {
    private String schemaQuery;
    private String selectQuery;
    private String insertQuery;
    private String updateQuery;
    private String deleteQuery;
    private String tableName;
    private Schema schema;

    public SimpleSqlDataQueryProvider(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    @JsonIgnore
    @Override
    public String getSchemaQuery() {
        if (schemaQuery == null) {
            schemaQuery = String.format("select * from %s limit 0", tableName);
        }
        return schemaQuery;
    }

    @Override
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    @JsonIgnore
    @Override
    public String getSnapshotQuery() {
        if (selectQuery == null) {
            selectQuery = String.format("select * from %s", tableName);
        }
        return selectQuery;
    }

    @JsonIgnore
    @Override
    public String getInsertQuery() {
        if (insertQuery == null) {
            String placeholders = "";
            if (schema.getWidth() > 1) {
                placeholders = Strings.repeat(",?", schema.getWidth() - 1);
            }

            String columnNames = "";
            boolean first = true;
            List<ColumnHolder> columnHolders = schema.getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                if (!first) {
                    columnNames += ",";
                }
                columnNames += columnHolder.getName();
                first = false;
            }

            insertQuery = "insert into " + tableName + " (" + columnNames + ") values (?" + placeholders + ")";
        }
        return insertQuery;
    }

    @Override
    public String getUpdateQuery(TableKeyDefinition tableKeyDefinition) {
        if (updateQuery == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("update ").append(tableName).append(" set ");
            boolean first = true;
            List<ColumnHolder> columnHolders = schema.getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                if (!first) {
                    builder.append(',');
                }
                builder.append(columnHolder.getName()).append("=?");
                first = false;
            }
            builder.append(" where ");
            appendWhereClause(tableKeyDefinition, builder);

            updateQuery = builder.toString();
        }
        return updateQuery;
    }

    @JsonIgnore
    @Override
    public String getDeleteQuery() {
        return "delete from " + tableName;
    }

    @Override
    public String getDeleteQuery(TableKeyDefinition tableKeyDefinition) {
        if (deleteQuery == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("delete from ").append(tableName).append(" where ");
            appendWhereClause(tableKeyDefinition, builder);

            deleteQuery = builder.toString();
        }
        return deleteQuery;
    }

    private void appendWhereClause(TableKeyDefinition tableKeyDefinition, StringBuilder builder) {
        boolean first = true;
        List<String> keys = tableKeyDefinition.getKeys();
        int count = keys.size();
        for (int i = 0; i < count; i++) {
            if (!first) {
                builder.append(" and ");
            }
            builder.append(keys.get(i)).append("=?");
            first = false;
        }
    }
}
