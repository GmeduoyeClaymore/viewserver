package io.viewserver.datasource;

import rx.Observable;
import rx.functions.FuncN;

import java.util.Collection;

public class ObservableUtils {
    public static <T> Observable<T> zip(Collection<Observable<T>> observables){
        if(observables.isEmpty()){
            return Observable.just(null);
        }
        FuncN<T> onCompletedAll = (FuncN<T>) objects -> {
            return null;
        };
        FuncN<T> onCompletedAll1 = onCompletedAll;
        return Observable.zip(observables, onCompletedAll1);
    }
}
