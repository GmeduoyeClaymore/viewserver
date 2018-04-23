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

package io.viewserver.operators;

import io.viewserver.Constants;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.schema.SchemaChange;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;

import java.util.List;

/**
 * Created by nickc on 30/09/2014.
 */
public class ChangeRecorder extends OperatorBase {
    private Input input;

    public ChangeRecorder(String name, IExecutionContext executionContext, ICatalog catalog) {
        super(name, executionContext, catalog);

        input = new Input(Constants.IN, this);
        addInput(input);
        register();
    }

    public IInput getInput() {
        return input;
    }

    private class Input extends InputBase {
        public Input(String name, IOperator owner) {
            super(name, owner);
        }

        @Override
        public void resetSchema() {
            log(getOwner().getName() + " - STATUS: SchemaReset");
            super.resetData();
        }

        @Override
        protected void onSchemaReset() {
            log(getOwner().getName() + " - SCHEMA: { " + getSchemaDetails() + " }");
        }

        @Override
        protected void onSchemaChange(SchemaChange schemaChange) {
            StringBuilder builder = new StringBuilder();
            builder.append(getOwner().getName()).append(" - SCHEMA CHANGE: { ");
            if (!schemaChange.getAddedColumns().isEmpty()) {
                builder.append("Add: { ");
                boolean first = true;
                for (ColumnHolder addedColumn : schemaChange.getAddedColumns()) {
                    if (!first) {
                        builder.append(", ");
                    }
                    first = false;
                    builder.append(addedColumn.getColumnId()).append(": ").append(addedColumn.getName()).append(" [").append(addedColumn.getType()).append("]");
                }
                builder.append(" } ");
            }
            if (!schemaChange.getRemovedColumns().isEmpty()) {
                builder.append("Remove: { ");
                boolean first = true;
                for (ColumnHolder removedColumn : schemaChange.getRemovedColumns()) {
                    if (!first) {
                        builder.append(", ");
                    }
                    first = false;
                    builder.append(removedColumn.getColumnId()).append(": ").append(removedColumn.getName());
                }
                builder.append(" } ");
            }
            builder.append(" }");
            log(builder.toString());
        }

        private String getSchemaDetails() {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            List<ColumnHolder> columnHolders = getProducer().getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                if (!first) {
                    builder.append(", ");
                }
                first = false;
                builder.append(columnHolder.getColumnId()).append(": ").append(columnHolder.getName()).append(" [").append(columnHolder.getType()).append("]");
            }
            return builder.toString();
        }

        @Override
        public void resetData() {
            log(getOwner().getName() + " - STATUS: DataReset");
            super.resetData();
        }

        @Override
        protected void onRowAdd(int row) {
            log(getOwner().getName() + " - ADD: { " + getRowDetails(row, null) + " }");
        }

        @Override
        protected void onRowUpdate(int row, IRowFlags rowFlags) {
            log(getOwner().getName() + " - UPD: { " + getRowDetails(row, rowFlags) + " }");
        }

        @Override
        protected void onRowRemove(int row) {
            log(getOwner().getName() + " - REM," + row);
        }

        private String getRowDetails(int row, IRowFlags rowFlags) {
            StringBuilder builder = new StringBuilder();
            builder.append("id = " + row);
            List<ColumnHolder> columnHolders = input.getProducer().getSchema().getColumnHolders();
            int count = columnHolders.size();
            for (int i = 0; i < count; i++) {
                ColumnHolder columnHolder = columnHolders.get(i);
                if (columnHolder == null) {
                    continue;
                }
                if (rowFlags != null && !rowFlags.isDirty(columnHolder.getColumnId())) {
                    continue;
                }
                builder.append(", ").append(columnHolder.getName()).append("=").append(ColumnHolderUtils.getValue(columnHolder, row));
            }
            return builder.toString();
        }

        private void log(String message) {
            System.out.println(message);
        }
    }
}
