package io.viewserver.messages.config;

import java.awt.*;
import java.util.List;

public interface IDimensionMapConfig<T> extends IOperatorConfig<T> {
    List<Dimension> getDimensions();
    void setDimensions(List<Dimension> dimensions);
    boolean removeInputColumns();
    void setRemoveInputColumns(boolean removeInputColumns);
    String getDataSourceName();
    void setDataSourceNames(String dataSourceName);
}
