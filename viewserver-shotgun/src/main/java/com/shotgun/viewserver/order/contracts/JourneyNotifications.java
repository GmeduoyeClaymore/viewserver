package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;

public interface JourneyNotifications extends OrderNotificationContact{
    default void notifyJobPartnerComplete(String orderId, String customerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,customerId,  "Shotgun partner has completed your job",  String.format("%s has  just marked your job as complete", user.getFirstName() + " " + user.getLastName()));
    }

    default void notifyJobStarted(String orderId, String customerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,customerId,  "Shotgun partner is on the way",  String.format("%s is on the way", user.getFirstName() + " " + user.getLastName()));
    }
}
