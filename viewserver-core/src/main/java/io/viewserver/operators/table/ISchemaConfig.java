package io.viewserver.datasource;

import java.util.List;

public interface ISchemaConfig {
    List<Column> getColumns();

    void setColumns(List<Column> columns);

    List<String> getKeyColumns();

    void setKeyColumns(List<String> keyColumns);
}
