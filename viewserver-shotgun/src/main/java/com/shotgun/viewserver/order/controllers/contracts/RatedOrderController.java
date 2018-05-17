package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.order.contracts.RatingNotifications;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserRating;
import com.shotgun.viewserver.user.UserTransformationController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

public interface RatedOrderController extends UserTransformationController, OrderUpdateController,RatingNotifications {

    @ControllerAction(path = "addOrUpdateRating", isSynchronous = true)
    default void addOrUpdateRating(UserRating rating) {
        String orderId = rating.getOrderId();
        UserRating.RatingType ratingType = rating.getRatingType();
        BasicOrder order = getOrderForId(orderId, BasicOrder.class);
        String userId = ratingType.equals(UserRating.RatingType.Customer) ? order.getCustomerUserId() : order.getPartnerUserId();
        rating.set("userId", getUserId());
        rating.set("title", order.getTitle());
        this.transform(userId,
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


}
