package io.viewserver.operators.rx;

import io.viewserver.core.IExecutionContext;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.internal.util.ActionSubscriber;
import rx.schedulers.Schedulers;

import java.util.concurrent.Executor;

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

    public static final Scheduler executionContextScheduler(IExecutionContext context, int delay){
        return Schedulers.from(new Executor() {
            @Override
            public void execute(Runnable command) {

                context.submit(command,delay);
            }
        });
    }

}


