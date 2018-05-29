package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;

public interface OrderNotificationContract {

    default void sendMessage(String orderId,String fromUserId, String toUserId, String title, String body, String urlSuffix, String imageUrl) {

        try {
            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId, urlSuffix))
                    .message(title, body)
                    .withPicture(imageUrl)
                    .withFromTo(fromUserId, toUserId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }
    default void sendMessage(String orderId,String fromUserId, String toUserId, String title, String body, boolean sendingToCustomer) {
        sendMessage(orderId,fromUserId,toUserId,title,body,sendingToCustomer ? "CustomerOrderDetail" : "PartnerOrderDetail", null);
    }

    default void sendMessage(String orderId, String toUserId, String title, String body, boolean isGoingToCustomer) {
        sendMessage(orderId, (String) ControllerContext.get("userId"), toUserId, title,body, isGoingToCustomer);
    }

    default void sendMessage(String orderId, String toUserId, String title, String body, String urlSuffix) {
        sendMessage(orderId, (String) ControllerContext.get("userId"), toUserId, title,body, urlSuffix, null);
    }
    default void sendMessage(String orderId, String toUserId, String title, String body, String urlSuffix,String imageUrl) {
        sendMessage(orderId, (String) ControllerContext.get("userId"), toUserId, title,body, urlSuffix, imageUrl);
    }





    default String createActionUri(String orderId, String urlSuffix){
        return String.format("shotgun://%s/%s", urlSuffix, orderId);
    }
    Logger getLogger();
    IMessagingController getMessagingController();
}
