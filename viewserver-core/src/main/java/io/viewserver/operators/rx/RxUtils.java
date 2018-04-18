package io.viewserver.operators.rx;

import io.viewserver.core.IExecutionContext;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.internal.util.ActionSubscriber;

public class RxUtils {

    public static final <T> Subscription subscribeOnExecutionContext(Observable<T> observable, IExecutionContext context, Action1<? super T> onNext, Action1<Throwable> onError) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        } else if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        } else {
            Action0 onCompleted = Actions.empty();
            return observable.subscribe(new ExecutionContextSubscriber<T>(context, onNext, onError, onCompleted));
        }
    }
}


