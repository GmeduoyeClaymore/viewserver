package io.viewserver.operators.table;

import io.viewserver.datasource.Column;

import java.util.List;

public interface ISchemaConfig {
    List<Column> getColumns();

    List<String> getKeyColumns();
}
