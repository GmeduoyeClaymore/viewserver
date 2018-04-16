package io.viewserver.operators.spread;

import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;

public class CsvSpreadFunction implements ISpreadFunction {
    @Override
    public String[] getValues(int row, ColumnHolder columnHolder) {
        String value = (String) ColumnHolderUtils.getValue(columnHolder,row);
        if(value == null){
            return null;
        }
        return value.split(",");
    }
}
