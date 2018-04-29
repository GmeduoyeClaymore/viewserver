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

    default void notifyHireItemOutForDelivery(HireOrder parentOrder) {
        User user = (User) ControllerContext.get("user");
        sendMessage(parentOrder.getOrderId(),parentOrder.getCustomerUserId(),  "Hire order out for delivery",   String.format("%s has just marked your order %s as out for delivery", user.getFirstName() + " " + user.getLastName(), parentOrder.getDescription()));
    }

    default void notifyItemOnHire(HireOrder parentOrder) {
        User user = (User) ControllerContext.get("user");
        sendMessage(parentOrder.getOrderId(),parentOrder.getPartnerUserId(),  "Hire order delivered",   String.format("%s has just marked your order %s as delivered", user.getFirstName() + " " + user.getLastName(), parentOrder.getDescription()));
        sendMessage(parentOrder.getOrderId(),parentOrder.getCustomerUserId(),  "Hire started",   String.format("%s has started. You should have the item till %s to avoid extra charges", parentOrder.getDescription(), parentOrder.getHireEndDate()));
    }

}
