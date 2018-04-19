package io.viewserver.messages.config;

import java.util.List;

public interface IDimensionMapConfig<T> extends IOperatorConfig<T> {
    List<Dimension> getDimensions();
    IDimensionMapConfig setDimensions(List<Dimension> dimensions);
    boolean removeInputColumns();
    IDimensionMapConfig setRemoveInputColumns(boolean removeInputColumns);
    String getDataSourceName();
    IDimensionMapConfig setDataSourceName(String dataSourceName);
}
