/*
 * Copyright 2016 Claymore Minds Limited and Niche Solutions (UK) Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.viewserver.reactor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.network.IPeerSession;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * Created by bemm on 06/10/2014.
 */
public interface IReactor {
    String getName();

    Executor getExecutor();

    IReactorCommandWheel getCommandWheel();

    void addLoopTask(Runnable task);

    void addLoopTask(Runnable task, int priority);

    void removeLoopTask(Runnable task);

    void wakeUp();

    void shutDown();

    void waitForShutdown();

    void scheduleTask(ITask task, long delay, long frequency);

    <V> void addCallback(ListenableFuture<V> future,
                         FutureCallback<? super V> callback);

    void start();

    default <T> Observable<T> scheduleTaskObservable(Callable<T> o, long delay, long frequency){
        return Observable.create(
                tEmitter -> scheduleTask(() -> {
                    try {
                        tEmitter.onNext(o.call());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },delay,frequency), Emitter.BackpressureMode.BUFFER
        );
    }

    default <T> Observable<T> scheduleObservable(Callable<Observable<T>> of, long delay, long frequency){
        return Observable.create(
                tEmitter -> scheduleTask(() -> {
                    try {
                        Observable<T> o = of.call();
                        o.subscribe(
                                el -> tEmitter.onNext(el),
                                ex -> tEmitter.onError(ex),
                                () -> tEmitter.onCompleted()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },delay,frequency), Emitter.BackpressureMode.BUFFER
        );
    }



}
