package com.shotgun.viewserver.login;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.user.UserAppStatus;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.*;
import com.shotgun.viewserver.user.User;
import com.shotgun.viewserver.user.UserStatus;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.*;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observable.ListenableFutureObservable;

import java.util.*;

import static com.shotgun.viewserver.ControllerUtils.getUserId;
import static com.shotgun.viewserver.user.UserController.waitForUser;

/**
 * Created by Gbemiga on 13/12/17.
 */
@Controller(name = "loginController")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private IDatabaseUpdater iDatabaseUpdater;
    private ICatalog systemcatalog;

    public LoginController(IDatabaseUpdater iDatabaseUpdater, ICatalog systemcatalog) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.systemcatalog = systemcatalog;
    }

    @ControllerAction(path = "login", isSynchronous = true)
    public String login(@ActionParam(name = "email", exampleValue = "Steven.Brown@email.com")String email, @ActionParam(name = "password", exampleValue = "driver")String password){

        ITable userTable = ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        int userRowId = getUserRow(userTable, email.toLowerCase());
        if(userRowId == -1){
            throw new RuntimeException("Unable to find user for email " + email);
        }
        String encryptedPassWord = (String)ControllerUtils.getColumnValue(userTable, "password", userRowId);

        if(ControllerUtils.validatePassword(password, encryptedPassWord)){
            String userId = (String)ControllerUtils.getColumnValue(userTable, "userId", userRowId);
            setUserId(userId);
            return userId;
        }else{
            throw new RuntimeException("Incorrect password supplied for user with email " + email);
        }
    }

    @ControllerAction(path = "setUserId", isSynchronous = false)
    public ListenableFuture<String> setUserId(String userId) {
        IPeerSession peerSession = ControllerContext.Current().getPeerSession();
        return ListenableFutureObservable.to(setUserIdObservable(userId, peerSession));
    }

    public Observable<String> setUserIdObservable(String userId, IPeerSession peerSession) {
        Observable datasources = waitForDataSources(UserDataSource.NAME, OrderDataSource.NAME, ContentTypeDataSource.NAME);
        return datasources.flatMap(obj -> systemcatalog.waitForOperatorAtThisPath(TableNames.USER_TABLE_NAME).cast(KeyedTable.class).
                flatMap(ut ->
                        waitForUser(userId, ut).map(
                                rec -> {
                                    setupContext(userId, peerSession);
                                    setUserOnline(userId).subscribe();
                                    return userId;
                                })
                ));
    }

    @ControllerAction(path = "background", isSynchronous = true)
    public void background() {
        setUserAppStatus(getUserId(), UserAppStatus.BACKGROUND);
    }

    @ControllerAction(path = "foreground", isSynchronous = true)
    public void foreground() {
        setUserAppStatus(getUserId(), UserAppStatus.FOREGROUND);
    }

    @ControllerAction(path = "logOut", isSynchronous = true)
    public ListenableFuture logOut() {
        String userId = getUserId();
        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("fcmToken", null);
        return ListenableFutureObservable.to(iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord));
    }

    private Observable<Boolean> setUserOnline(String userId) {
        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("online", true)
                .addValue("userStatus", UserStatus.ONLINE.name())
                .addValue("userAppStatus", UserAppStatus.FOREGROUND);

        return iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord);
    }

    private ListenableFuture setUserOffLine(String userId) {
        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("online", false)
                .addValue("userStatus", UserStatus.OFFLINE.name())
                .addValue("userAppStatus", UserAppStatus.BACKGROUND);

        return ListenableFutureObservable.to(iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord));
    }

    private ListenableFuture setUserAppStatus(String userId, UserAppStatus appStatus) {
        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("userAppStatus", appStatus);

        return ListenableFutureObservable.to(iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord));
    }

    private String setupContext(String userId,IPeerSession session) {
        session.onDisconnect().take(1).subscribe(c-> setUserOffLine(userId));
        ControllerContext.set("userId", userId,session);
        ControllerContext.setFactory("user", () -> getUser(userId), session);
        ControllerContext.setFactory("now", () -> new Date().getTime(), session);
        return userId;
    }



    private User getUser(String userId) {
        KeyedTable keyedTable = (KeyedTable) systemcatalog.getOperatorByPath(TableNames.USER_TABLE_NAME);
        HashMap<String, Object> userRow = keyedTable.getRowObject(new TableKey(userId));
        if(userRow == null){
            throw new RuntimeException(String.format("Unable to find user for id %s",userId));
        }
        return JSONBackedObjectFactory.create(userRow, User.class);
    }

    //TODO - this full table scan won't scale will need to do something better at some point
    public int getUserRow(ITable userTable, String loginEmail){
        IOutput output = userTable.getOutput();
        IRowSequence rows = (output.getAllRows());

        while(rows.moveNext()){
            String email = (String) ControllerUtils.getColumnValue(userTable, "email", rows.getRowId());
            if(email != null && email.toLowerCase().equals(loginEmail)){
                return rows.getRowId();
            }
        }
        return -1;
    }

    public Observable<Object> waitForDataSources(String... dataSourceNames){
        logger.info("Waiting for data");
        List<Observable<OperatorEvent>> waitedForDataSources = new ArrayList<Observable<OperatorEvent>>();
        for(String dataSource : dataSourceNames){
            waitedForDataSources.add(waitForDataSource(dataSource));
        }
        return rx.Observable.zip(waitedForDataSources.toArray(new Observable[0]), objects -> null).take(1).map(c-> {
            logger.info("!!!! All datasources initialized !!!!");
            return null;
        });
    }

    private Observable<OperatorEvent> waitForDataSource(String dataSource) {
        return systemcatalog.getOperatorByPath("datasources").getOutput("out").observable().filter(c-> isInitialized(c,dataSource)).take(1);
    }

    private static boolean isInitialized(OperatorEvent ev, String name) {
        if(!ev.getEventType().equals(EventType.ROW_ADD) && !ev.getEventType().equals(EventType.ROW_UPDATE)){
            return false;
        }
        HashMap<String,Object> result = (HashMap<String,Object>)ev.getEventData();
        boolean b = "INITIALIZED".equals(result.get("status")) && name.equals(result.get("name"));
        if(b){
            logger.info("Data source {} initialized",name);
        }
        return b;
    }





}
