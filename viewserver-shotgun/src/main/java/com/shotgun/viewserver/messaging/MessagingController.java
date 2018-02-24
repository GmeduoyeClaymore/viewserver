package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.*;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.TableUpdater;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.KeyedTable;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Scheduler;
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
public class MessagingController {

    private static final Logger logger = LoggerFactory.getLogger(MessagingController.class);

    private ListeningExecutorService service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat("messaging-controller-executor-%d").build()));

    private static String MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";

    private MessagingApiKey messagingApiKey;
    private TableUpdater tableUpdater;

    public MessagingController(MessagingApiKey messagingApiKey, TableUpdater tableUpdater) {
        this.messagingApiKey = messagingApiKey;
        this.tableUpdater = tableUpdater;
    }

    public void sendMessage(AppMessage message){
        sendPayload(message.toSimpleMessage());
    }

    public ListenableFuture sendMessageToUser(String userId, AppMessage message){
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        return ListenableFutureObservable.to(waitForUser(userId, userTable).observeOn(Schedulers.from(service)).map(rec -> {
            String currentToken = (String) rec.get("fcmToken");
            String format = String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, userId, currentToken);
            logger.info(format);
            sendPayload(message.toSimpleMessage());
            return format;
        }));
    }

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
            tableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
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
}
