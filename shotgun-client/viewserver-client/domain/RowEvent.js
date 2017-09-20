export default class RowEvent{
    constructor(type, columnValues, rowId) {
        this.type = type;
        this.columnValues = columnValues;
        this.rowId = rowId;
    }

    static EventType = {
        ADD: 0,
        UPDATE: 1,
        REMOVE: 2
    }
}