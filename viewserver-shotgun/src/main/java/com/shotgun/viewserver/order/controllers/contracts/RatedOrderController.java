package com.shotgun.viewserver.order.controllers.contracts;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.order.contracts.RatingNotifications;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserRating;
import com.shotgun.viewserver.user.UserTransformationController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import rx.Observable;
import rx.observable.ListenableFutureObservable;


import static com.shotgun.viewserver.ControllerUtils.getUserId;

public interface RatedOrderController extends UserTransformationController, OrderUpdateController,RatingNotifications {

    @ControllerAction(path = "addOrUpdateRating", isSynchronous = true)
    default ListenableFuture addOrUpdateRating(UserRating rating) {
        String orderId = rating.getOrderId();
        UserRating.RatingType ratingType = rating.getRatingType();
        Observable<BasicOrder> orderObservable = getOrderForId(orderId, BasicOrder.class);
        return ListenableFutureObservable.to(
                orderObservable.map(
                        order -> {
                            String userId = ratingType.equals(UserRating.RatingType.Customer) ? order.getCustomerUserId() : order.getPartnerUserId();
                            rating.set("fromUserId", getUserId());
                            rating.set("title", order.getTitle());
                            return this.transform(userId,
                                    user -> {
                                        UserRating userRating = user.addRating(rating);
                                        order.set("rating" + ratingType.name(), userRating);
                                        updateOrderRecord(order);
                                        return true;
                                    },
                                    user -> {
                                        if(ratingType.equals(UserRating.RatingType.Customer)){
                                            notifyPartnerHasEnteredRating(orderId,userId);
                                        }else{
                                            notifyCustomerHasEnteredRating(orderId,userId);
                                        }
                                    }, User.class);
                        }
                )
        );

    }


}
