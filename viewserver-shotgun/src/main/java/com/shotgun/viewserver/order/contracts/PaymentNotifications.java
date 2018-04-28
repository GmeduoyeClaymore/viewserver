package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;

public interface PaymentNotifications extends OrderNotificationContact{
    default void notifyJobCompleted(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, "Shotgun job completed and paid", String.format("%s has  just marked your job complete and paid", user.getFirstName() + " " + user.getLastName()));
    }
    default void notifyPaymentStageStarted(String orderId, String customerId, String description) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, customerId, "Shotgun payment stage started", String.format("%s has  just marked part of your job \"%s\" started", user.getFirstName() + " " + user.getLastName(), description));
    }

    default void notifyPaymentStageComplete(String orderId, String customerId, String description) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, customerId, "Shotgun Job Stage Complete", String.format("%s has  just marked part of your job \"%s\" complete. Please log in to pay", user.getFirstName() + " " + user.getLastName(), description));
    }

    default void notifyPaymentStagePaid(String orderId, String partnerId, String description) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, String.format("Shotgun Job Stage Paid"), String.format("%s has  just paid for stage \"%s\" complete.", user.getFirstName() + " " + user.getLastName(), description));
    }



}
