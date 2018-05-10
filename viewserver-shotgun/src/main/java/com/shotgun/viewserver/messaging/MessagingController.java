package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.*;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserPersistenceController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
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

    public MessagingController(MessagingApiKey messagingApiKey, IDatabaseUpdater iDatabaseUpdater, ICatalog catalog) {
        this.messagingApiKey = messagingApiKey;
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.catalog = catalog;
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
            if(currentToken == null){
                String result = "Not sending message to {} as cannot yet find an fcm token for that user. Message marked as pending and will be sent when user registers token";
                logger.info(result);
                user.addPendingMessage(message);
                return result;
            }
            String format = String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken);
            logger.info(String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken));
            boolean sendRemotely = !user.getOnline();
            if(sendRemotely){
                sendPayload(message.toSimpleMessage());
            }
            persistMessage(message, sendRemotely);
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
    public Logger getLogger() {
        return logger;
    }
}



