package io.viewserver.operators.spread;

import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvSpreadFunction implements ISpreadFunction {
    @Override
    public List<Column> getColumns() {
        return null;
    }

    @Override
    public List<Map.Entry<Column, Object[]>> getValues(int row, ColumnHolder columnHolder) {
        String value = (String) ColumnHolderUtils.getValue(columnHolder,row);
        if(value == null){
            return null;
        }
        List<Map.Entry<Column,Object[]>> result = new ArrayList<>();
        result.add(new HashMap.SimpleEntry(new Column(columnHolder.getName() + "_csv",ContentType.String),value.split(",")));
        return result;
    }
}
