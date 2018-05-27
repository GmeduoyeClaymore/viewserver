package com.shotgun.viewserver.order.controllers.contracts;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.domain.BasicOrder;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import rx.Observable;
import rx.observable.ListenableFutureObservable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface OrderTransformationController extends OrderUpdateController{

    interface ITranformation<TOrder>{
        TOrder call(TOrder order);
    }

    interface IAsyncPredicate<TOrder>{
        Observable<Boolean> call(TOrder order);
    }

    default <T extends BasicOrder> ListenableFuture transform(String orderId, Predicate<T> tranformation, Class<T> orderClass){
        return transform(orderId,tranformation,c->{}, orderClass);
    }


    default <T extends BasicOrder> Observable<Object> transformObservable(String orderId, Predicate<T> tranformation, Class<T> orderClass){
        return transformObservable(orderId,tranformation,c->{}, orderClass);
    }


    default <T extends BasicOrder> ListenableFuture transform(String orderId, Predicate<T> tranformation, Consumer<T> afterTransform, Class<T> orderClass){
        return ListenableFutureObservable.to(transformObservable(orderId, tranformation, afterTransform, orderClass));
    }

    default <T extends BasicOrder> Observable<Object> transformObservable(String orderId, Predicate<T> tranformation, Consumer<T> afterTransform, Class<T> orderClass) {
        ControllerContext context = ControllerContext.Current();
        return getOrderForId(orderId, orderClass).subscribeOn(ControllerContext.Scheduler(context)).flatMap(
                order -> {
                    try {
                        ControllerContext.create(context);
                        if (tranformation.test(order)) {
                            Observable<Boolean> observable = updateOrderRecordObservable(order);
                            return observable.observeOn(ControllerContext.Scheduler(context)).map(res -> {
                                afterTransform.accept(order);
                                return res;
                            });
                        }
                    }finally {
                        ControllerContext.closeStatic();
                    }
                    return Observable.just(order);

                }
        );
    }

    default <T extends BasicOrder> ListenableFuture transformAsync(String orderId,IAsyncPredicate<T> tranformation, Consumer<T> afterTransform, Class<T> orderClass){
        return ListenableFutureObservable.to(transformAsyncObservable(orderId, tranformation, afterTransform, orderClass));
    }

    default <T extends BasicOrder> Observable<Object> transformAsyncObservable(String orderId, IAsyncPredicate<T> tranformation, Consumer<T> afterTransform, Class<T> orderClass) {
        ControllerContext context = ControllerContext.Current();
        return getOrderForId(orderId, orderClass).flatMap(
            order -> {
                return tranformation.call(order).observeOn(ControllerContext.Scheduler(context)).flatMap(
                        res -> {
                            if (res) {
                                Observable<Boolean> observable = updateOrderRecordObservable(order);
                                return observable.observeOn(ControllerContext.Scheduler(context)).map(res2 -> {
                                    afterTransform.accept(order);
                                    return res2;
                                });
                            }
                            return Observable.just(order).observeOn(ControllerContext.Scheduler(context));
                        }
                );
            }
        );
    }


}
