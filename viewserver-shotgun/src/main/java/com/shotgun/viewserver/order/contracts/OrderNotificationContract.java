package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;

public interface OrderNotificationContract {

    default void sendMessage(String orderId,String fromUserId, String toUserId, String title, String body) {
        try {
            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(title, body)
                    .withFromTo(fromUserId, toUserId)
                    .build();
            getMessagingController().sendMessageToUser(builder);
        }catch (Exception ex){
            getLogger().error("There was a problem sending the notification", ex);
        }
    }

    default void sendMessage(String orderId, String toUserId, String title, String body) {
        sendMessage(orderId, (String) ControllerContext.get("userId"), toUserId, title,body);
    }


    default String createActionUri(String orderId){
        return String.format("shotgun://PartnerOrderDetail/%s", orderId);
    }
    Logger getLogger();
    IMessagingController getMessagingController();
}
