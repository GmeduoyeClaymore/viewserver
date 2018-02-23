package com.shotgun.viewserver.login;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.user.User;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.IInput;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.InputBase;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.reactor.ITask;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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

    private static <T> void setValue(ITable userTable, int userRowId,String columnName, Consumer<T> valueSetter){
        Object columnValue = ControllerUtils.getColumnValue(userTable, columnName, userRowId);
        if(columnValue != null){
            valueSetter.accept((T) columnValue);
        }
    }

    @ControllerAction(path = "setUserId", isSynchronous = true)
    public ListenableFuture setUserId(String userId) {
        KeyedTable userTable = (KeyedTable) ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        SettableFuture result = SettableFuture.create();
        try {
            int userRowId = userTable.getRow(new TableKey(userId));
            if (userRowId == -1) {

                InputBase userIdInput = new InputBase("userAdditionListener", userTable) {
                    @Override
                    protected void onRowAdd(int row) {
                        String userId = (String) ControllerUtils.getColumnValue(userTable, "userId", row);
                        if (userId == userId) {
                            setupContext(userId, userTable, userRowId);
                            result.set(userId);
                            logger.info("Found user " + userId + " after waiting");
                            userTable.getOutput().unplug(this);
                        }
                    }
                };
                userTable.getOutput().plugIn(userIdInput);
                userTable.getExecutionContext().getReactor().scheduleTask(new ITask() {
                    @Override
                    public void execute() {
                        if (result.isDone()) {
                            result.setException(new RuntimeException("Unable to find user id " + userId));
                            try {
                                logger.info("Unable to find user " + userId + " after waiting. NOT WAITING ANY MORE");
                                userTable.getOutput().unplug(userIdInput);
                            } catch (Exception ex) {
                                logger.error("Problem unlistening for user" + userId + "");
                            }
                        }

                    }
                }, 5000, -1);
            }
            setupContext(userId, userTable, userRowId);
            result.set(userId);
        }catch(Exception ex){
            result.setException(ex);
        }
        return result;
    }

    private void setupContext(String userId, KeyedTable userTable, int userRowId) {
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
