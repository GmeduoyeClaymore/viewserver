package com.shotgun.viewserver.order.contracts;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;

public interface PhoneCallNotifications extends OrderNotificationContract {
    default void notifyPartnerHasEnteredRating(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, "Shotgun rating entered", String.format("%s has just rated a job you were involved in", user.getFirstName() + " " + user.getLastName()),true);
    }

    default void notifyCustomerHasEnteredRating(String orderId, String customerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, customerId, "Shotgun rating entered", String.format("%s has just rated a job you were involved in", user.getFirstName() + " " + user.getLastName()),false);
    }
}
