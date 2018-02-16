package com.shotgun.viewserver.login;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.user.User;
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

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Created by Gbemiga on 13/12/17.
 */
@Controller(name = "loginController")
public class LoginController {

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

    private static <T> void setValue(ITable userTable, int userRowId,String columnName, Consumer<T> valueSetter){
        Object columnValue = ControllerUtils.getColumnValue(userTable, columnName, userRowId);
        if(columnValue != null){
            valueSetter.accept((T) columnValue);
        }
    }

    @ControllerAction(path = "setUserId", isSynchronous = true)
    public void setUserId(String userId) {
        KeyedTable userTable = (KeyedTable) ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        int userRowId = userTable.getRow(new TableKey(userId));
        if(userRowId == -1){
            throw new RuntimeException(String.format("Unable to find user for id %s",userId));
        }
        ControllerContext.set("userId", userId);
        User user = new User();
        setValue(userTable,userRowId,"userId", v ->  user.setUserId((String) v));
        setValue(userTable,userRowId,"chargePercentage", v ->  user.setChargePercentage((Integer) v));
        setValue(userTable,userRowId,"contactNo", v ->  user.setContactNo((String) v));
        setValue(userTable,userRowId,"created", v ->  user.setCreated(new Date((Long) v)));
        setValue(userTable,userRowId,"dob", v ->  user.setDob(new Date((Long) v)));
        setValue(userTable,userRowId,"email", v ->  user.setEmail((String) v));
        setValue(userTable,userRowId,"firstName", v ->  user.setFirstName((String) v));
        setValue(userTable,userRowId,"lastModified", v ->  user.setLastModified(new Date((Long) v)));
        setValue(userTable,userRowId,"lastName", v ->  user.setLastName((String) v));
        setValue(userTable,userRowId,"password", v ->  user.setPassword((String) v));
        setValue(userTable,userRowId,"selectedContentTypes", v ->  user.setSelectedContentTypes((String) v));
        setValue(userTable,userRowId,"stripeAccountId", v ->  user.setStripeAccountId((String) v));
        setValue(userTable,userRowId,"stripeCustomerId", v ->  user.setStripeCustomerId((String) v));
        setValue(userTable,userRowId,"stripeDefaultSourceId", v ->  user.setStripeDefaultSourceId((String) v));
        setValue(userTable,userRowId,"type", v ->  user.setType((String) v));
        ControllerContext.set("user", user);
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
}
