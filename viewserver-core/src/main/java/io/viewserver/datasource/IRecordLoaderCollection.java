package io.viewserver.datasource;

import io.viewserver.datasource.IRecordLoader;
import rx.Observable;

import java.util.Map;

public interface IRecordLoaderCollection {
    Map<String,IRecordLoader> getDataLoaders() ;

    void close();

    default Observable start(){
        return Observable.just(true);
    }
}
