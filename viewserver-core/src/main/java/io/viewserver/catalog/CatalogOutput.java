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

package io.viewserver.catalog;

import io.viewserver.operators.IOperator;
import io.viewserver.operators.OutputBase;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.ColumnStringBase;


public class CatalogOutput extends OutputBase {
    public static final String NAME_COLUMN = "name";
    public static final String TYPE_COLUMN = "type";
    public static final String PATH_COLUMN = "path";
    private CatalogHolder catalogHolder;

    public CatalogOutput(String name, IOperator owner, CatalogHolder catalogHolder) {
        super(name, owner);
        this.catalogHolder = catalogHolder;
        getSchema().addColumn(ColumnHolderUtils.createColumnHolder(NAME_COLUMN, io.viewserver.schema.column.ColumnType.String));
        getSchema().addColumn(ColumnHolderUtils.createColumnHolder(TYPE_COLUMN, io.viewserver.schema.column.ColumnType.String));
        getSchema().addColumn(ColumnHolderUtils.createColumnHolder(PATH_COLUMN, io.viewserver.schema.column.ColumnType.String));
    }

    @Override
    public void clearSchema() {
    }

    @Override
    public void clearData() {
    }

    private class NameColumn extends ColumnStringBase {
        public NameColumn() {
            super(NAME_COLUMN);
        }

        @Override
        public String getString(int row) {
            IOperator operator = catalogHolder.getOperatorForRow(row);
            return operator.getName();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }

    private class TypeColumn extends ColumnStringBase {
        private TypeColumn() {
            super(TYPE_COLUMN);
        }

        @Override
        public String getString(int row) {
            IOperator operator = catalogHolder.getOperatorForRow(row);
            return operator.getClass().getName();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }

    private class PathColumn extends ColumnStringBase {
        private PathColumn() {
            super(PATH_COLUMN);
        }

        @Override
        public String getString(int row) {
            IOperator operator = catalogHolder.getOperatorForRow(row);
            return operator.getPath();
        }

        @Override
        public String getPreviousString(int row) {
            return null;
        }

        @Override
        public boolean supportsPreviousValues() {
            return false;
        }
    }
}

