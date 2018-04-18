package com.shotgun.viewserver.setup;

import io.viewserver.datasource.IRecordLoader;

import java.util.Map;

public interface IDataLoaderCollection {
    Map<String,IRecordLoader> getDataLoaders() ;
}
