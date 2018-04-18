package io.viewserver.operators.dimension;

import io.viewserver.datasource.Dimension;

import java.util.List;

public interface IDimensionMapConfig{
    List<Dimension> getDimensions();
    boolean removeInputColumns();
    String getDataSourceName();
}
