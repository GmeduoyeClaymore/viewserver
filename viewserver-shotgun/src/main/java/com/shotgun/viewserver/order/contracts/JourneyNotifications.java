package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.order.domain.JourneyOrder;
import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;

public interface JourneyNotifications extends OrderNotificationContract {
    default void notifyJourneyComplete(String orderId, JourneyOrder order) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,order.getCustomerUserId(),  "Shotgun partner has completed your job",  String.format("%s has  just marked your job as complete", user.getFirstName() + " " + user.getLastName()));
    }

    default void notifyJourneyStarted(String orderId, JourneyOrder order) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,order.getCustomerUserId(),  "Shotgun partner is on the way",  String.format("%s is on the way", user.getFirstName() + " " + user.getLastName()));
    }
}
