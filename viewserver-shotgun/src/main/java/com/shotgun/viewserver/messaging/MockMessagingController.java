package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.servercomponents.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.observable.ListenableFutureObservable;
import rx.schedulers.Schedulers;

import java.util.HashMap;
import java.util.concurrent.Executors;

import static com.shotgun.viewserver.user.UserController.waitForUser;

@Controller(name = "messagingController")
public class MockMessagingController implements IMessagingController {

    private static final Logger logger = LoggerFactory.getLogger(MessagingController.class);

    private ListeningExecutorService service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat("messaging-controller-executor-%d").build()));

    private static String MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private HashMap<String, AppMessage> messagesBySender = new HashMap<>();
    private HashMap<String, AppMessage> messagesByRecipient = new HashMap<>();
    private IDatabaseUpdater databaseUpdater;

    public MockMessagingController(IDatabaseUpdater databaseUpdater) {
        this.databaseUpdater = databaseUpdater;
    }

    public void clear(){
        messagesByRecipient.clear();
        messagesBySender.clear();
    }

    @Override
    public void sendMessage(AppMessage message){
        sendPayload(message);
    }

    @Override
    public ListenableFuture sendMessageToUser(AppMessage message){
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        return ListenableFutureObservable.to(waitForUser(message.getToUserId(), userTable).observeOn(Schedulers.from(service)).map(rec -> {
            String currentToken = (String) rec.get("fcmToken");
            String format = String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken);
            logger.info(String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken));
            message.setTo(currentToken);
            sendPayload(message);
            return format;
        }));
    }

    @Override
    @ControllerAction(path = "updateUserToken")
    public ListenableFuture updateUserToken(String token){

        if(token == null || "".equals(token)){
            throw new RuntimeException("Invalid empty token specified");
        }
        String userId = (String) ControllerContext.get("userId");
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        return ListenableFutureObservable.to(waitForUser(userId, userTable).map(rec -> {
            String currentToken = (String) rec.get("fcmToken");
            logger.info("Updating token \"{}\" to \"{}\"",currentToken, token);
            Record userRecord = new Record()
                    .addValue("userId", userId)
                    .addValue("fcmToken", token);
            databaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
            return userRecord;
        }));
    }


    public void sendPayload(AppMessage message) {
        this.messagesByRecipient.put(message.getToUserId(),message);
        this.messagesBySender.put(message.getFromUserId(), message);
    }
}
