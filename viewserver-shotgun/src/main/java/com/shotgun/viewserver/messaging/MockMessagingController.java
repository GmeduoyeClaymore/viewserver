package com.shotgun.viewserver.messaging;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
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

    private static final Logger logger = LoggerFactory.getLogger(MockMessagingController.class);

    private ListeningExecutorService service =  MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat("messaging-controller-executor-%d").build()));

    private HashMap<String, AppMessage> messagesBySender = new HashMap<>();
    private HashMap<String, AppMessage> messagesByRecipient = new HashMap<>();
    private IDatabaseUpdater databaseUpdater;
    private ICatalog catalog;

    public MockMessagingController(IDatabaseUpdater databaseUpdater, ICatalog catalog) {
        this.databaseUpdater = databaseUpdater;
        this.catalog = catalog;
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
        KeyedTable userTable = (KeyedTable) catalog.getOperatorByPath(TableNames.USER_TABLE_NAME);
        return ListenableFutureObservable.to(waitForUser(message.getToUserId(), userTable).observeOn(Schedulers.from(service)).map(rec -> {
            String currentToken = (String) rec.get("fcmToken");
            message.set("to",currentToken);
            persistMessage(message, false).subscribe();
            String format = String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken);
            logger.info(String.format("Sending message \"%s\" to \"%s\" token \"%s\"", message, message.getToUserId(), currentToken));

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
        KeyedTable userTable = (KeyedTable) catalog.getOperatorByPath(TableNames.USER_TABLE_NAME);
        return ListenableFutureObservable.to(waitForUser(userId, userTable).map(rec -> {
            String currentToken = (String) rec.get("fcmToken");
            logger.info("Updating token \"{}\" to \"{}\"",currentToken, token);
            Record userRecord = new Record()
                    .addValue("userId", userId)
                    .addValue("fcmToken", token);
            databaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord, IRecord.UPDATE_LATEST_VERSION).subscribe();
            return userRecord;
        }));
    }

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return databaseUpdater;
    }


    public void sendPayload(AppMessage message) {
        this.messagesByRecipient.put(message.getToUserId(),message);
        this.messagesBySender.put(message.getFromUserId(), message);
    }
}
