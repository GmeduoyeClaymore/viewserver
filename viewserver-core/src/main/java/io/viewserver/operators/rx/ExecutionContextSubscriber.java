package io.viewserver.operators.rx;

import io.viewserver.core.IExecutionContext;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

public final class ExecutionContextSubscriber<T> extends Subscriber<T> {
    private IExecutionContext context;
    final Action1<? super T> onNext;
    final Action1<Throwable> onError;
    final Action0 onCompleted;

    public ExecutionContextSubscriber(IExecutionContext context, Action1<? super T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        this.context = context;
        this.onNext = onNext;
        this.onError = onError;
        this.onCompleted = onCompleted;
    }

    public void onNext(T t) {
        context.submit(() -> this.onNext.call(t),0);
    }

    public void onError(Throwable e) {
        this.onError.call(e);
    }

    public void onCompleted() {
        this.onCompleted.call();
    }
}
