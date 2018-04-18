package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.ListenableFuture;
import io.viewserver.controller.ControllerAction;

public interface IMessagingController {
    void sendMessage(AppMessage message);

    ListenableFuture sendMessageToUser(AppMessage message);

    @ControllerAction(path = "updateUserToken")
    ListenableFuture updateUserToken(String token);

}
