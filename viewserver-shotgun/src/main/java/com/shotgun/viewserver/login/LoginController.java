package com.shotgun.viewserver.login;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.user.User;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.network.IPeerSession;
import io.viewserver.operators.*;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.reactor.ITask;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.observable.ListenableFutureObservable;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.shotgun.viewserver.user.UserController.waitForUser;

/**
 * Created by Gbemiga on 13/12/17.
 */
@Controller(name = "loginController")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @ControllerAction(path = "login", isSynchronous = true)
    public String login(@ActionParam(name = "email")String email, @ActionParam(name = "password")String password){

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
    public ListenableFuture setUserId(String userId) {
        rx.Observable<IOperator> userTable = ControllerUtils.getOperatorObservable(TableNames.USER_TABLE_NAME);
        IPeerSession peerSession = ControllerContext.Current().getPeerSession();
        return ListenableFutureObservable.to(
                userTable.cast(KeyedTable.class).
                            flatMap( ut -> waitForUser(userId, ut).map(rec -> setupContext(rec,peerSession))));
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
        setValue(userRecord,"type", v ->  user.setType((String) v));
        ControllerContext.set("user", user, session);
        return user;
    }

    public int getUserRow(ITable userTable, String loginEmail){
        IOutput output = userTable.getOutput();
        IRowSequence rows = (output.getAllRows());

        while(rows.moveNext()){
            String email = (String)ControllerUtils.getColumnValue(userTable, "email", rows.getRowId());
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

}
