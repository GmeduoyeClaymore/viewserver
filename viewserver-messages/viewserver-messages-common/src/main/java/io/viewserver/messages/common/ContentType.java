package io.viewserver.messages.common;

public enum ContentType {
    Bool(ColumnType.Boolean),
    NullableBool(ColumnType.NullableBoolean),
    Byte(ColumnType.Byte),
    Short(ColumnType.Short),
    Int(ColumnType.Integer),
    Long(ColumnType.Long),
    Float(ColumnType.Float),
    Double(ColumnType.Double),
    String(ColumnType.String),
    Date(ColumnType.Long),
    DateTime(ColumnType.Long);

    private ColumnType columnType;

    ContentType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public ColumnType getColumnType() {
        return columnType;
    }
}
