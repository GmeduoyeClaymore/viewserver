package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.HireOrder;
import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;

public interface HireNotifications extends OrderNotificationContract {
    default void notifyItemReady(String orderId, BasicOrder hireOrder) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, hireOrder.getCustomerUserId(), "Item awaiting collection", String.format("%s has just told us your item is ready and awaiting collection", user.getFirstName() + " " + user.getLastName()));
    }

    default void notifyItemAwaitingCollection(String orderId, BasicOrder hireOrder) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId, hireOrder.getCustomerUserId(), "Item awaiting collection", String.format("%s has just told us your item is awaiting collection", user.getFirstName() + " " + user.getLastName()));
    }

}
