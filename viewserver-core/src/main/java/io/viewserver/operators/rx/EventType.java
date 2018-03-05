package io.viewserver.operators.rx;

public enum EventType{
    COLUMN_ADD,
    COLUMN_REMOVE,
    ROW_ADD,
    ROW_UPDATE,
    ROW_REMOVE,
    SNAPSHOT_COMPLETE,
    DATA_RESET,
    SCHEMA_RESET_REQUESTED,
    SCHEMA_RESET,
}
