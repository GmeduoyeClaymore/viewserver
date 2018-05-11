package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;

public interface OrderNotificationContract {

    default void sendMessage(String orderId,String fromUserId, String toUserId, String title, String body, String urlSuffix) {

        try {
            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId, urlSuffix))
                    .message(title, body)
                    .withFromTo(fromUserId, toUserId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }
    default void sendMessage(String orderId,String fromUserId, String toUserId, String title, String body, boolean sendingToCustomer) {
        sendMessage(orderId,fromUserId,toUserId,title,body,sendingToCustomer ? "CustomerOrderDetail" : "PartnerOrderDetail");
    }

    default void sendMessage(String orderId, String toUserId, String title, String body, boolean isGoingToCustomer) {
        sendMessage(orderId, (String) ControllerContext.get("userId"), toUserId, title,body, isGoingToCustomer);
    }

    default void sendMessage(String orderId, String toUserId, String title, String body, String urlSuffix) {
        sendMessage(orderId, (String) ControllerContext.get("userId"), toUserId, title,body, urlSuffix);
    }


    default String createActionUri(String orderId, String urlSuffix){
        return String.format("shotgun://%s/%s", urlSuffix, orderId);
    }
    Logger getLogger();
    IMessagingController getMessagingController();
}
