package com.shotgun.viewserver.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.NamedThreadFactory;
import com.shotgun.viewserver.ShotgunTableUpdater;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.PhoneNumberStatuses;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Created by Gbemiga on 17/01/18.
 */
@Controller(name = "messagingController")
public class MessagingController {

    private static final Logger logger = LoggerFactory.getLogger(MessagingController.class);

    private ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1,new NamedThreadFactory("messagingThread")));

    private static String MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";

    private MessagingApiKey messagingApiKey;
    private ShotgunTableUpdater shotgunTableUpdater;

    public MessagingController(MessagingApiKey messagingApiKey, ShotgunTableUpdater shotgunTableUpdater) {
        this.messagingApiKey = messagingApiKey;
        this.shotgunTableUpdater = shotgunTableUpdater;
    }

    @ControllerAction(path = "sendMessage")
    public void sendMessage(AppMessage message){
        sendPayload(message.toSimpleMessage());
    }

    @ControllerAction(path = "sendMessageToUser")
    public void sendMessageToUser(@ActionParam(name = "userId")String userId, @ActionParam(name = "message")AppMessage message){
        String currentToken = getTokenForUser(userId);
        message.setTo(currentToken);
        service.submit(() -> {
            logger.info("Sending message \"{}\" to \"{}\" token \"{}\"", message, userId, currentToken);
            sendPayload(message.toSimpleMessage());
        });
    }

    @ControllerAction(path = "updateUserToken")
    public void updateUserToken(String token){

        if(token == null || "".equals(token)){
            throw new RuntimeException("Invalid empty token specified");
        }
        String userId = (String) ControllerContext.get("userId");
        String currentToken = getTokenForUser(userId);

        logger.info("Updating token \"{}\" to \"{}\"",currentToken, token);

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("fcmToken", token);

        shotgunTableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
    }

    public String getTokenForUser(String userId){
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);

        int currentRow = userTable.getRow(new TableKey(userId));
        if(currentRow == -1){
            throw new RuntimeException(String.format("Unable to find user id \"%s\" in the user table",userId));
        }
        return ControllerUtils.getColumnValue(userTable, "fcmToken", currentRow) + "";
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
