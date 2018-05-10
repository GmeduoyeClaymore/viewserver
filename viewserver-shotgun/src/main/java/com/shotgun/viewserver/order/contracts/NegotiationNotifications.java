package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.order.domain.HireOrder;
import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;

public interface NegotiationNotifications extends OrderNotificationContract {

    default void notifyJobBackOnTheMarket(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,partnerId,  "Shotgun job back on the market", String.format("%s job has just come back onto the market" , user.getFirstName() + " " + user.getLastName() ));
    }

    default void notifyJobBackOnTheMarket(String orderId,String customerId,String partnerId) {
        sendMessage(orderId,customerId, partnerId,  "Shotgun job back on the market", "A job you responded to has just come back onto the market" );
    }

    default void notifyJobAccepted(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,partnerId,  "Shotgun offer accepted", String.format("%s has  just accepted your offer", user.getFirstName() + " " + user.getLastName()));
    }
    default void notifyJobCancelled(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,partnerId,  "Job canclled", String.format("%s has just cancelled a job that you responded to", user.getFirstName() + " " + user.getLastName()));
    }

    default void notifyJobRejected(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,partnerId,  "Shotgun offer rejected", String.format("%s has  just rejected your offer", user.getFirstName() + " " + user.getLastName()));
    }

    default void notifyJobResponded(String orderId, String customerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,customerId,  "Shotgun job response",  String.format("%s has  just responded to a job that you posted", user.getFirstName() + " " + user.getLastName()));
    }

    default void notifyResponseCancelled(String orderId, String customerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,customerId,  "Shotgun response cancelled",  String.format("%s has  just cancelled response to job", user.getFirstName() + " " + user.getLastName()));
    }

    default void notifyJobAssigned(String orderId, String partnerId) {
        User user = (User) ControllerContext.get("user");
        sendMessage(orderId,partnerId,  "Shotgun job assigned to you",   String.format("%s has  just assigned a job to you in shotgun", user.getFirstName() + " " + user.getLastName()));
    }
}



