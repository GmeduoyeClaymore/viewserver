package com.shotgun.viewserver.login;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.ITable;
import io.viewserver.schema.Schema;
import io.viewserver.schema.column.ColumnHolder;
import io.viewserver.schema.column.ColumnHolderUtils;

/**
 * Created by Gbemiga on 13/12/17.
 */
@Controller(name = "loginController")
public class LoginController {

    @ControllerAction(path = "login", isSynchronous = true)
    public String login(@ActionParam(name = "email")String email, @ActionParam(name = "password")String password){

        ITable userTable = ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        int userRowId = getUserRow(userTable, email.toLowerCase());
        String encryptedPassWord = (String)ControllerUtils.getColumnValue(userTable, "password", userRowId);

        if(ControllerUtils.validatePassword(password, encryptedPassWord)){
            String userId = (String)ControllerUtils.getColumnValue(userTable, "userId", userRowId);
            ControllerContext.set("userId", userId);
            return userId;
        }else{
            throw new RuntimeException("Incorrect password supplied for user with email " + email);
        }
    }

    private int getUserRow(ITable userTable, String loginEmail){
        IOutput output = userTable.getOutput();
        IRowSequence rows = (output.getAllRows());

        while(rows.moveNext()){
            String email = (String)ControllerUtils.getColumnValue(userTable, "email", rows.getRowId());

            if(email.equals(loginEmail)){
                return rows.getRowId();
            }
        }
        throw new RuntimeException("Unable to find user for email " + loginEmail);
    }
}
