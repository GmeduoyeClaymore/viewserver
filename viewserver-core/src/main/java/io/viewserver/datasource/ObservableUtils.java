package io.viewserver.datasource;

import rx.Observable;
import rx.functions.FuncN;

import java.util.Collection;

public class ObservableUtils {
    public static Observable<Object> zip(Collection<Observable<Object>> observables){
        if(observables.isEmpty()){
            return Observable.just(true);
        }
        FuncN<?> onCompletedAll = (FuncN<Object>) objects -> {
            return true;
        };
        FuncN<?> onCompletedAll1 = onCompletedAll;
        return Observable.zip(observables, onCompletedAll1);
    }
}
