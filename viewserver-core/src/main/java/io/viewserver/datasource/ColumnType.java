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

/**
 * Created by nickc on 13/10/2014.
 */
public enum ColumnType {
    Bool(io.viewserver.schema.column.ColumnType.Bool),
    NullableBool(io.viewserver.schema.column.ColumnType.NullableBool),
    Byte(io.viewserver.schema.column.ColumnType.Byte),
    Short(io.viewserver.schema.column.ColumnType.Short),
    Int(io.viewserver.schema.column.ColumnType.Int),
    Long(io.viewserver.schema.column.ColumnType.Long),
    Float(io.viewserver.schema.column.ColumnType.Float),
    Double(io.viewserver.schema.column.ColumnType.Double),
    String(io.viewserver.schema.column.ColumnType.String),
    Date(io.viewserver.schema.column.ColumnType.Long),
    DateTime(io.viewserver.schema.column.ColumnType.Long);

    private io.viewserver.schema.column.ColumnType columnType;

    ColumnType(io.viewserver.schema.column.ColumnType columnType) {
        this.columnType = columnType;
    }

    public io.viewserver.schema.column.ColumnType getColumnType() {
        return columnType;
    }
}
