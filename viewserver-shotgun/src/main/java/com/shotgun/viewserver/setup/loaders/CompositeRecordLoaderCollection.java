package com.shotgun.viewserver.setup.loaders;

import io.viewserver.datasource.IRecordLoader;
import io.viewserver.datasource.IRecordLoaderCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class CompositeRecordLoaderCollection  implements IRecordLoaderCollection {
    private Callable<IRecordLoaderCollection>[] collections;
    private HashMap<String, IRecordLoader> loaderMap;

    public CompositeRecordLoaderCollection(Callable<IRecordLoaderCollection>... collections){

        this.collections = collections;

    }

    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        loaderMap = new HashMap<>();
        for(Callable<IRecordLoaderCollection> factory : collections){
            try {
                loaderMap.putAll(factory.call().getDataLoaders());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return loaderMap;
    }

    @Override
    public void close() {
        if(loaderMap != null){
            loaderMap.values().forEach(c->c.close());
        }
    }
}
