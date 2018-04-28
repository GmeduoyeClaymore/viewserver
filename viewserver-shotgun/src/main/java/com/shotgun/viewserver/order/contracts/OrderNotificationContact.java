package com.shotgun.viewserver.order.contracts;

import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;

public interface OrderNotificationContact {

    default void sendMessage(String orderId, String toUserId, String title, String body) {
        try {
            String userId = (String)ControllerContext.get("userId");
            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId))
                    .message(title, body)
                    .withFromTo(userId, toUserId)
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
