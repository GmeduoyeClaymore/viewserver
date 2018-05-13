package com.shotgun.viewserver.order.contracts;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;

public interface UserNotificationContract{

    default void notifyRelationshipStatus(String userId, String targetUserId, String status) {
        String formattedStatus = status.toLowerCase();
        String fromUserName = getUsername(userId);
        AppMessage builder = new AppMessageBuilder().withDefaults()
                .withAction(createUserActionUri(userId, "UserDetail"))
                .withFromTo(userId, targetUserId)
                .message(String.format("Shotgun friend request", formattedStatus), String.format("Shotgun user %s has %s your friendship", fromUserName, formattedStatus)).build();
        ListenableFuture future = getMessagingController().sendMessageToUser(builder);
    }

    default void notifyMissedCall(String userId, String targetUserId) {
        String fromUserName = getUsername(userId);
        AppMessage builder = new AppMessageBuilder().withDefaults()
                .withAction(createUserActionUri(userId, "UserDetail"))
                .withFromTo(userId, targetUserId)
                .message("Shotgun missed call", String.format("Shotgun user %s has just attempted to call you", fromUserName)).build();
        ListenableFuture future = getMessagingController().sendMessageToUser(builder);
    }

    default String getUsername(String userId) {
        KeyedTable userTable = getUserTable();
        String firstName = (String) ControllerUtils.getColumnValue(userTable, "firstName", userId);
        String lastName = (String) ControllerUtils.getColumnValue(userTable, "lastName", userId);
        return firstName + " " + lastName;
    }

    default String createUserActionUri(String userId, String urlSuffix){
        return String.format("shotgunu://%s/%s", urlSuffix, userId);
    }

    KeyedTable getUserTable();

    Logger getLogger();
    IMessagingController getMessagingController();
}
