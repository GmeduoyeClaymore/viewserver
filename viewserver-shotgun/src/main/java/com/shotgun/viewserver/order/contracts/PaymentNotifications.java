package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;

public interface PaymentNotifications extends OrderNotificationContract {
    default void notifyJobCompleted(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, "Shotgun job completed and paid", String.format("%s has  just marked your job complete and paid", user.getFirstName() + " " + user.getLastName()), false);
    }
    default void notifyPaymentStageStarted(String orderId, String customerId, String description) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, customerId, "Shotgun payment stage started", String.format("%s has  just marked part of your job \"%s\" started", user.getFirstName() + " " + user.getLastName(), description),"CustomerOrderDetail~PaymentStages");
    }

    default void notifyPaymentStageComplete(String orderId, String customerId, String description) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, customerId, "Shotgun Job Stage Complete", String.format("%s has  just marked part of your job \"%s\" complete. Please log in to pay", user.getFirstName() + " " + user.getLastName(), description),"CustomerOrderDetail~PaymentStages");
    }

    default void notifyPaymentStagePaid(String orderId, String partnerId, String description) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, String.format("Shotgun Job Stage Paid"), String.format("%s has  just paid for stage \"%s\" complete.", user.getFirstName() + " " + user.getLastName(), description),"PartnerOrderDetail~PaymentStages");
    }


    default void notifyPaymentStageAdded(String orderId, String partnerId, String description, String title) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, String.format("Shotgun Payment stage added"), String.format("%s has  just added a payment  stage \"%s\" to job \"%s\"", user.getFirstName() + " " + user.getLastName(), description, title),"PartnerOrderDetail~PaymentStages");
    }




}
