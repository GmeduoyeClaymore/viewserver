package io.viewserver.operators.spread;

import io.viewserver.schema.column.ColumnHolder;

public interface ISpreadFunction {
    String[] getValues(int row, ColumnHolder columnHolder);
}
