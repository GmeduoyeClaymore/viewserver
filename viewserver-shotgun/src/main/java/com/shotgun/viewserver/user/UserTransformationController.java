package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.controllers.contracts.OrderTransformationController;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import rx.Observable;
import rx.observable.ListenableFutureObservable;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface UserTransformationController extends UserPersistenceController{

    interface ITranformation<TUSer>{
        TUSer call(TUSer order);
    }

    interface IAsyncPredicate<TOrder>{
        Observable<Boolean> call(TOrder order);
    }


    default <T extends User> ListenableFuture transform(String userId, Predicate<T> tranformation, Class<T> userClass){
        if(userId == null){
            throw new RuntimeException("User id is required");
        }
        return transform(userId,tranformation,c->{}, userClass);
    }

    default <T extends User> Observable<Object> transformObservable(String userId, Predicate<T> tranformation, Class<T> userClass){
        if(userId == null){
            throw new RuntimeException("User id is required");
        }
        return transformObservable(userId,tranformation,c->{}, userClass);
    }

    default <T extends User> ListenableFuture transformAsync(String userId, UserTransformationController.IAsyncPredicate<T> tranformation, Consumer<T> afterTransform, Class<T> userClass){
        return ListenableFutureObservable.to(transforAsyncObservable(userId, tranformation, afterTransform, userClass));
    }

    default <T extends User> Observable<Object> transforAsyncObservable(String userId, IAsyncPredicate<T> tranformation, Consumer<T> afterTransform, Class<T> userClass) {
        ControllerContext context = ControllerContext.Current();
        return getUserForId(userId, userClass).flatMap(
                order -> {
                    return tranformation.call(order).observeOn(ControllerContext.Scheduler(context)).flatMap(
                            res -> {
                                if (res) {
                                    Observable<String> observable = addOrUpdateUserObservable(order, null);
                                    return observable.observeOn(ControllerContext.Scheduler(context)).map(
                                            res2 -> {
                                                afterTransform.accept(order);
                                                return res2;
                                            }
                                    );
                                }
                                return Observable.just(order).observeOn(ControllerContext.Scheduler(context));
                            }
                    );


                }
        );
    }


    default <T extends User> ListenableFuture transform(String userId, Predicate<T> tranformation, Consumer<T> afterTransform, Class<T> userClass){
        return ListenableFutureObservable.to(transformObservable(userId, tranformation, afterTransform, userClass));
    }

    default <T extends User> Observable<Object> transformObservable(String userId, Predicate<T> tranformation, Consumer<T> afterTransform, Class<T> userClass) {
        ControllerContext context = ControllerContext.Current();
        return getUserForId(userId, userClass).observeOn(ControllerContext.Scheduler(context)).flatMap(
                user -> {
                    try {
                        ControllerContext.create(context);
                        if (tranformation.test(user)) {
                            Observable<String> observable = addOrUpdateUserObservable(user, null);
                            return observable.observeOn(ControllerContext.Scheduler(context)).map(res -> {
                                afterTransform.accept(user);
                                return res;
                            });
                        }
                    }finally {
                        ControllerContext.closeStatic();
                    }
                    return Observable.just(user);

                }
        );
    }


}
