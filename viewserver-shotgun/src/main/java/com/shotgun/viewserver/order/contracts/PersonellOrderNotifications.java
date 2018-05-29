package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.user.User;
import com.stripe.model.Order;
import io.viewserver.controller.ControllerContext;

public interface PersonellOrderNotifications extends OrderNotificationContract{
    default void notifyCustomerAddedImage(String orderId, String partnerId, String title, String imageUrl) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, partnerId, "Image added to job", String.format("%s has  just added an image to job \"%s\"", user.getFirstName() + " " + user.getLastName(), title),"PartnerOrderDetail~Photos", imageUrl);
    }

    default void notifyPartnerAddedImage(String orderId, String customerId, String title, String imageUrl) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, customerId, "Image added to job", String.format("%s has  just added an image to job \"%s\"", user.getFirstName() + " " + user.getLastName(), title),"CustomerOrderDetail~Photos", imageUrl);
    }


}
