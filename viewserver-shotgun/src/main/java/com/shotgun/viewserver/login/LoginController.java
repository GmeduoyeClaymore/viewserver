package com.shotgun.viewserver.login;

import com.shotgun.viewserver.ControllerUtils;
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

    private static String USER_TABLE_NAME = "/datasources/user/user";

    @ControllerAction(path = "login", isSynchronous = true)
    public String login(@ActionParam(name = "username")String username,@ActionParam(name = "password")String password){

        ITable userTable = ControllerUtils.getTable(USER_TABLE_NAME);
        IOutput output = userTable.getOutput();
        IRowSequence rows = (output.getAllRows());

        Schema schema = output.getSchema();
        ColumnHolder emailCol = schema.getColumnHolder("email");
        ColumnHolder userIdCol = schema.getColumnHolder("userId");
        while(rows.moveNext()){
            String email = ColumnHolderUtils.getValue(emailCol, rows.getRowId()).toString();
            if(email.equals(username)){
                String userId = ColumnHolderUtils.getValue(userIdCol, rows.getRowId()).toString();
                ControllerContext.set("userId", userId);
                return userId;
            }
        } 
        throw new RuntimeException("Unable to find user for username " + username);
    }
}
