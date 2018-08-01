package io.viewserver.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.FuncN;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CompositeRecordLoaderCollection  implements IRecordLoaderCollection {
    private Callable<IRecordLoaderCollection>[] collectionFactories;
    private List<IRecordLoaderCollection> collections;
    private HashMap<String, IRecordLoader> loaderMap;

    private Logger logger = LoggerFactory.getLogger(CompositeRecordLoaderCollection.class);

    public CompositeRecordLoaderCollection(Callable<IRecordLoaderCollection>... collectionFactories){
        this.collectionFactories = collectionFactories;
    }

    @Override
    public Map<String, IRecordLoader> getDataLoaders() {
        loaderMap = new HashMap<>();
        for(IRecordLoaderCollection collection : getCollections()){
            try {
                loaderMap.putAll(collection.getDataLoaders());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return loaderMap;
    }

    private List<IRecordLoaderCollection> getCollections() {
        if(collections == null){
            collections = Arrays.stream(collectionFactories).map(fac -> {
                try {
                    return fac.call();
                } catch (Exception e) {
                    logger.error("Problem creating collection",e);
                    return null;
                }
            }).filter(res -> res != null).collect(Collectors.toList());
        }
        return collections;
    }


    @Override
    public Observable start() {
        return ObservableUtils.zip(collections.stream().map(c-> (Observable<Object>)c.start()).collect(Collectors.toList()));
    }

    @Override
    public void close() {
        if(loaderMap != null){
            loaderMap.values().forEach(c-> close(c));
        }
    }

    private void close(IRecordLoader c) {
        try {
            c.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}

