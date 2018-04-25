package io.viewserver.operators.spread;

import io.viewserver.datasource.Column;
import io.viewserver.datasource.ContentType;
import io.viewserver.schema.column.ColumnHolder;

import java.util.List;
import java.util.Map;

public interface ISpreadFunction {
    List<Map.Entry<Column,Object[]>> getValues(int row, ColumnHolder columnHolder);
}
