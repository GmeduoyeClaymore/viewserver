package io.viewserver.operators.table;

import io.viewserver.datasource.Column;

import java.util.List;

public interface ISchemaConfig {
    TableKeyDefinition getTableKeyDefinition();

    List<Column> getColumns();

    List<String> getKeyColumns();
}
