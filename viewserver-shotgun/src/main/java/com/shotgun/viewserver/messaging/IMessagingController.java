package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.controller.ControllerAction;

public interface IMessagingController {
    void sendMessage(AppMessage message);

    ListenableFuture sendMessageToUser(AppMessage message);

    @ControllerAction(path = "updateUserToken")
    ListenableFuture updateUserToken(String token);

    default void persistMessage(AppMessage message) {
        Record messageRec = new Record();
        messageRec.addValue("messageId", ControllerUtils.generateGuid());
        messageRec.addValue("fromUserId", message.getFromUserId());
        messageRec.addValue("toUserId", message.getToUserId());
        messageRec.addValue("message", message);

        getDatabaseUpdater().addOrUpdateRow(TableNames.MESSAGES_TABLE_NAME, UserDataSource.getDataSource().getSchema(), messageRec);
    }

    IDatabaseUpdater getDatabaseUpdater();

}
