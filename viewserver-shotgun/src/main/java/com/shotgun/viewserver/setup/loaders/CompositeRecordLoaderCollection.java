package com.shotgun.viewserver.setup.loaders;

import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.IRecordLoaderCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class CompositeRecordLoaderCollection  implements IRecordLoaderCollection {
    private Callable<IRecordLoaderCollection>[] collections;

    public CompositeRecordLoaderCollection(Callable<IRecordLoaderCollection>... collections){

        this.collections = collections;

    }

    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        Map<String,IRecordLoader> loaderMap = new HashMap<>();
        for(Callable<IRecordLoaderCollection> factory : collections){
            try {
                loaderMap.putAll(factory.call().getDataLoaders());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return loaderMap;
    }
}
