package io.viewserver.operators.rx;

import io.viewserver.operators.IOutput;
import io.viewserver.schema.column.ColumnFlags;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.IRowFlags;

import java.util.HashMap;
import java.util.List;

import static io.viewserver.core.Utils.fromArray;

public class OperatorEvent{
    private EventType eventType;
    private Object eventData;

    public OperatorEvent(EventType eventType, Object eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Object getEventData() {
        return eventData;
    }

    public void setEventData(Object eventData) {
        this.eventData = eventData;
    }

    public static HashMap<String,Object> getRowDetails(IOutput producer, int row, IRowFlags rowFlags) {
        if(producer == null){
            return null;
        }
        HashMap<String,Object> result = new HashMap<>();
        List<ColumnHolder> columnHolders = producer.getSchema().getColumnHolders();
        int count = columnHolders.size();
        for (int i = 0; i < count; i++) {
            ColumnHolder columnHolder = columnHolders.get(i);


            if (columnHolder == null) {
                continue;
            }
            if (rowFlags != null && !rowFlags.isDirty(columnHolder.getColumnId() )&& !columnHolder.getMetadata().isFlagged(ColumnFlags.KEY_COLUMN)) {
                continue;
            }

            result.put(columnHolder.getName(), ColumnHolderUtils.getValue(columnHolder, row, true));
        }
        return result;
    }
}
