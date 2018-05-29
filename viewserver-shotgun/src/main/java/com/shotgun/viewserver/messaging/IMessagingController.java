package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.MessagesDataSource;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.controller.ControllerAction;
import rx.Observable;

import java.util.Date;

public interface IMessagingController {
    void sendMessage(AppMessage message);

    ListenableFuture sendMessageToUser(AppMessage message);

    @ControllerAction(path = "updateUserToken")
    ListenableFuture updateUserToken(String token);

    default Observable<Boolean> persistMessage(AppMessage message, boolean sentRemotely) {
        Record messageRec = new Record();
        messageRec.addValue("messageId", ControllerUtils.generateGuid());
        messageRec.addValue("fromUserId", message.getFromUserId());
        messageRec.addValue("toUserId", message.getToUserId());
        messageRec.addValue("title", message.getTitle());
        messageRec.addValue("picture", message.getPicture());
        messageRec.addValue("sentTime", new Date());
        messageRec.addValue("sentRemotely",sentRemotely);
        messageRec.addValue("message", message);

        return getDatabaseUpdater().addOrUpdateRow(TableNames.MESSAGES_TABLE_NAME, MessagesDataSource.getDataSource().getSchema(), messageRec);
    }

    IDatabaseUpdater getDatabaseUpdater();

}
