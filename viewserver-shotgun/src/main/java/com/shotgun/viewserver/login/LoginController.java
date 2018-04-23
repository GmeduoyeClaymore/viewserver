package com.shotgun.viewserver.login;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.IDatabaseUpdater;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observable.ListenableFutureObservable;

import java.util.*;
import java.util.function.Consumer;

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
    public String login(@ActionParam(name = "email", exampleValue = "1John.Customer@email.com")String email, @ActionParam(name = "password", exampleValue = "customer")String password){

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

    @ControllerAction(path = "setUserId", isSynchronous = true)
    public ListenableFuture<Object> setUserId(String userId) {
        rx.Observable datasources = waitForDataSources(UserDataSource.NAME, OrderDataSource.NAME, ContentTypeDataSource.NAME, UserRelationshipDataSource.NAME, OrderItemsDataSource.NAME);
        IPeerSession peerSession = ControllerContext.Current().getPeerSession();
        return ListenableFutureObservable.to(
                datasources.flatMap(obj -> systemcatalog.getOperatorObservable(TableNames.USER_TABLE_NAME).cast(KeyedTable.class).
                flatMap(ut ->
                        waitForUser(userId, ut).map(
                                rec -> {
                                    User user = setupContext(rec, peerSession);
                                    setUserOnline(user, ut).subscribe();
                                    return user;
                                })
                )));
    }

    private Observable<Boolean> setUserOnline(User user, KeyedTable table) {
        Record userRecord = new Record()
                .addValue("userId", user.getUserId())
                .addValue("userStatus", UserStatus.ONLINE.name());

        return iDatabaseUpdater.scheduleAddOrUpdateRow(table, "user", userRecord);
    }

    private User setupContext(Map<String,Object> userRecord, IPeerSession session) {
        ControllerContext.set("userId", userRecord.get("userId"),session);
        User user = new User();
        setValue(userRecord,"userId", v ->  user.setUserId((String) v));
        setValue(userRecord,"chargePercentage", v ->  user.setChargePercentage((Integer) v));
        setValue(userRecord,"contactNo", v ->  user.setContactNo((String) v));
        setValue(userRecord,"created", v ->  user.setCreated(new Date((Long) v)));
        setValue(userRecord,"dob", v ->  user.setDob(new Date((Long) v)));
        setValue(userRecord,"email", v ->  user.setEmail((String) v));
        setValue(userRecord,"firstName", v ->  user.setFirstName((String) v));
        setValue(userRecord,"lastModified", v ->  user.setLastModified(new Date((Long) v)));
        setValue(userRecord,"lastName", v ->  user.setLastName((String) v));
        setValue(userRecord,"password", v ->  user.setPassword((String) v));
        setValue(userRecord,"selectedContentTypes", v ->  user.setSelectedContentTypes((String) v));
        setValue(userRecord,"stripeAccountId", v ->  user.setStripeAccountId((String) v));
        setValue(userRecord,"stripeCustomerId", v ->  user.setStripeCustomerId((String) v));
        setValue(userRecord,"stripeDefaultSourceId", v ->  user.setStripeDefaultSourceId((String) v));
        setValue(userRecord,"statusMessage", v ->  user.setStatusMessage((String) v));
        setValue(userRecord,"type", v ->  user.setType((String) v));
        ControllerContext.set("user", user, session);
        return user;
    }

    //TODO - this full table scan won't scale will need to do something better at some point
    public int getUserRow(ITable userTable, String loginEmail){
        IOutput output = userTable.getOutput();
        IRowSequence rows = (output.getAllRows());

        while(rows.moveNext()){
            String email = ((String)ControllerUtils.getColumnValue(userTable, "email", rows.getRowId())).toLowerCase();
            if(email != null && email.equals(loginEmail)){
                return rows.getRowId();
            }
        }
        return -1;
    }


    private static <T> void setValue(Map<String,Object> row,String columnName,Consumer<T> valueSetter){
        Object columnValue = row.get(columnName);
        if(columnValue != null){
            valueSetter.accept((T) columnValue);
        }
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
        return systemcatalog.getOperator("datasources").getOutput("out").observable().filter(c-> isInitialized(c,dataSource)).take(1);
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
