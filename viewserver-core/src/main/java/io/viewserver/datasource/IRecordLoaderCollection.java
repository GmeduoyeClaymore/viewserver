package io.viewserver.datasource;

import io.viewserver.datasource.IRecordLoader;

import java.util.Map;

public interface IRecordLoaderCollection {
    Map<String,IRecordLoader> getDataLoaders() ;

    void close();
}
