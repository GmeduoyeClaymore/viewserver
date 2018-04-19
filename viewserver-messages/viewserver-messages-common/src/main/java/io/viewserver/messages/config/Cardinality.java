package io.viewserver.messages.config;

import io.viewserver.messages.common.ColumnType;

public enum Cardinality {
    Boolean(ColumnType.Boolean),
    Byte(ColumnType.Byte),
    Short(ColumnType.Short),
    Int(ColumnType.Integer);

    private ColumnType columnType;

    Cardinality(ColumnType columnType) {
        this.columnType = columnType;
    }

    public ColumnType getColumnType() {
        return columnType;
    }
}
