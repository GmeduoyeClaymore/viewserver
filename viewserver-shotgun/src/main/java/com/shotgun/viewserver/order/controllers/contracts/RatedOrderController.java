package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.order.contracts.PaymentNotifications;
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
    default void addOrUpdateRating(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating, @ActionParam(name = "comments") String comments, @ActionParam(name = "ratingType") UserRating.RatingType ratingType) {
        BasicOrder order = getOrderForId(orderId, BasicOrder.class);
        String userId = ratingType.equals(UserRating.RatingType.Customer) ? order.getCustomerUserId() : order.getPartnerUserId();

        this.transform(userId,
                user -> {
                    UserRating userRating = user.addRating(getUserId(), orderId, rating, comments, ratingType);
                    order.set("rating" + ratingType.name(), userRating);
                    updateOrderRecord(order);
                    return true;
                },
                user -> {
                    notifyUserRated(orderId,userId);
                }, User.class);
    }


}
