package io.viewserver.datasource;

import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.operators.table.TableKeyDefinition;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import io.viewserver.schema.column.chunked.ChunkedColumnStorage;

import java.util.List;

public class RecordUtils{

    public static void addRecordToTableOperator(KeyedTable operator, IRecord rec) {
        TableKeyDefinition tableKeyDefinition = operator.getTableKeyDefinition();
        if(tableKeyDefinition == null ){
            throw new RuntimeException(String.format("Cannot map record as no key columns are defined on schema - %s", rec));
        }
        String[] elements = new String[tableKeyDefinition.size()];
        for(String col : tableKeyDefinition.getKeys()){
            String keyElement = rec.getString(col);
            if(keyElement == null){
                throw new RuntimeException(String.format("Cannot map record as one of key element %s is empty %s",keyElement, rec));
            }
            elements[elements.length] = keyElement;
        }
        int row = operator.getRow(new TableKey(elements));
        for(ColumnHolder holder : operator.getOutput().getSchema().getColumnHolders()){
            ColumnHolderUtils.setValue(holder,row, rec.getValue(holder.getName()));
        }
    }
}
