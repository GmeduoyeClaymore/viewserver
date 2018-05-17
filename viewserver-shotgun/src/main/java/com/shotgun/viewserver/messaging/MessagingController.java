package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.*;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserAppStatus;
import com.shotgun.viewserver.user.UserPersistenceController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.operators.table.KeyedTable;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.observable.ListenableFutureObservable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static com.shotgun.viewserver.user.UserController.waitForUser;

/**
 * Created by Gbemiga on 17/01/18.
 */
@Controller(name = "messagingController")
public class MessagingController implements IMessagingController, UserPersistenceController {

    private static final Logger logger = LoggerFactory.getLogger(MessagingController.class);

    private ListeningExecutorService service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat("messaging-controller-executor-%d").build()));

    private static String MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";

    private MessagingApiKey messagingApiKey;
    private IDatabaseUpdater iDatabaseUpdater;
    private ICatalog catalog;
    private ConcurrentHashMap<String,String> tokenToUserMap;

    public MessagingController(MessagingApiKey messagingApiKey, IDatabaseUpdater iDatabaseUpdater, ICatalog catalog) {
        tokenToUserMap = new ConcurrentHashMap<>();
        this.messagingApiKey = messagingApiKey;
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.catalog = catalog;
        catalog.waitForOperatorAtThisPath(TableNames.USER_TABLE_NAME).subscribe(
                c-> c.getOutput("out").observable("userId","fcmToken").subscribe(
                        tk -> recordUserToken(tk)
                )
        );
    }

    private void recordUserToken(OperatorEvent tk) {
        HashMap eventData = (HashMap) tk.getEventData();
        if(tk.getEventType().equals(EventType.ROW_ADD) || tk.getEventType().equals(EventType.ROW_UPDATE)){
            if(eventData.get("fcmToken") != null){
                tokenToUserMap.put((String)eventData.get("fcmToken"), (String)eventData.get("userId"));
                return;
            }
        }
    }

    @Override
    public void sendMessage(AppMessage message){
        sendPayload(message.toSimpleMessage());
    }

    @Override
    public ListenableFuture sendMessageToUser(AppMessage message){
        KeyedTable userTable = (KeyedTable) catalog.getOperatorByPath(TableNames.USER_TABLE_NAME);

        if(message.getFromUserId() == null){
            throw new RuntimeException("From user id must be specified");
        }
        if(message.getToUserId() == null){
            throw new RuntimeException("To user id must be specified");
        }


        return ListenableFutureObservable.to(waitForUser(message.getToUserId(), userTable).observeOn(Schedulers.from(service)).map(rec -> {
            String currentToken = (String) rec.get("fcmToken");
            message.set("to",currentToken);

            User user = getUserForId(message.getToUserId(),User.class);

            boolean sendRemotely = isSendRemotely(user);
            persistMessage(message, sendRemotely);
            if(currentToken == null && isSendRemotely(user)){
                String result = "Not sending message to {} as cannot yet find an fcm token for that user. Message marked as pending and will be sent when user registers token";
                logger.info(result);
                user.addPendingMessage(message);
                return result;
            }
            String format = String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken);
            logger.info(String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken));
            if(sendRemotely){
                sendPayload(message.toSimpleMessage());
            }

            return format;
        }));
    }

    public boolean isSendRemotely(User user) {
        return !user.getOnline() || !UserAppStatus.FOREGROUND.equals(user.getUserAppStatus());
    }


    @Override
    @ControllerAction(path = "updateUserToken")
    public ListenableFuture updateUserToken(String token){

        if(token == null || "".equals(token)){
            throw new RuntimeException("Invalid empty token specified");
        }
        String userId = (String) ControllerContext.get("userId");
        String existingUserForToken = tokenToUserMap.get(token);

        if(userId.equals(existingUserForToken)){
            logger.info("User is already assigned this token aborting");
            return Futures.immediateFuture(null);
        }
        if(existingUserForToken != null){
            Record userRecord = new Record()
                    .addValue("userId", existingUserForToken)
                    .addValue("fcmToken", null);
            iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord);
        }
        KeyedTable userTable = (KeyedTable) catalog.getOperatorByPath(TableNames.USER_TABLE_NAME);
        return ListenableFutureObservable.to(waitForUser(userId, userTable).map(rec -> {
            String currentToken = (String) rec.get("fcmToken");
            logger.info("Updating token \"{}\" to \"{}\"",currentToken, token);
            Record userRecord = new Record()
                    .addValue("userId", userId)
                    .addValue("fcmToken", token);
            iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord);

            User user = (User) ControllerContext.get("user");
            if(user.getPendingMessages() != null){
                for(AppMessage message : user.getPendingMessages()){
                    message.set("to",token);
                    logger.info("Sending pending message {}", message);
                    sendPayload(message.toSimpleMessage());
                }
                user.clearPendingMessages();
                addOrUpdateUser(user, null);
            }
            return userRecord;
        }));
    }


    public void sendPayload(String payload) {
        Map<String,String> headers = new HashMap<>();
        logger.info("Sending message remotely {}", payload);
        headers.put("Authorization", "key=" + this.messagingApiKey.getApiKey());
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            String json = ControllerUtils.postToURL(MESSAGE_URL, payload, httpClient, headers);
            HashMap<String, Object> response = ControllerUtils.mapDefault(json);
            Object status = response.get("success");
            if ( status == null || !new Integer(1).equals(status)){
                throw new RuntimeException("Failed to send notification response is \"" + json+ "\"");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public IDatabaseUpdater getDatabaseUpdater(){
        return iDatabaseUpdater;
    }

    @Override
    public KeyedTable getUserTable() {
        return (KeyedTable) this.catalog.getOperatorByPath(TableNames.USER_TABLE_NAME);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}



