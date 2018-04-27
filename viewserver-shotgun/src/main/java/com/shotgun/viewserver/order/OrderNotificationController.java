package com.shotgun.viewserver.order;

import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.user.User;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;

public interface OrderNotificationController{

    default void notifyJobBackOnTheMarket(String orderId, String partnerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job back on the market"),"Job has just come back onto the market" )
                    .withFromTo(user.getUserId(),partnerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }



    default void notifyJobCompleted(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job completed and paid"), String.format("%s has  just marked your job complete and paid", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }

    default void notifyJobAccepted(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job assigned to you"), String.format("%s has  just accepted your offer", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }

    default void notifyJobRejected(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun offer rejected"), String.format("%s has  just rejected your offer", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }

    default void notifyJobPartnerComplete(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job started"), String.format("%s has  just marked your job as complete", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }



    default void notifyJobStarted(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job started"), String.format("%s has  just started your job", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }


    default void notifyJobResponded(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job assigned to you"), String.format("%s has  just responded to a job that you posted", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }

    default void notifyResponseCancelled(String orderId, String customerId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun response cancelled"), String.format("%s has  just cancelled response to job", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),customerId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }

    default void notifyJobAssigned(String orderId, String driverId) {
        try {
            User user = (User) ControllerContext.get("user");

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(String.format("Shotgun job assigned to you"), String.format("%s has  just assigned a job to you in shotgun", user.getFirstName() + " " + user.getLastName()))
                    .withFromTo(user.getUserId(),driverId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }

    default String createActionUri(String orderId){
        return String.format("shotgun://DriverOrderDetail/%s", orderId);
    }

    Logger getLogger();
    IMessagingController getMessagingController();

}