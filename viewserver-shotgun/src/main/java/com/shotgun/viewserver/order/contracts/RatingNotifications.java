package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;

public interface RatingNotifications extends OrderNotificationContract {
    default void notifyPartnerHasEnteredRating(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, "Shotgun rating entered", String.format("%s has just rated a job you were involved in", user.getFirstName() + " " + user.getLastName()),true);
    }

    default void notifyCustomerHasEnteredRating(String orderId, String customerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, customerId, "Shotgun rating entered", String.format("%s has just rated a job you were involved in", user.getFirstName() + " " + user.getLastName()),false);
    }
}
