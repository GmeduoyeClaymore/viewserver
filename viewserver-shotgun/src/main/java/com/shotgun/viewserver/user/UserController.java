package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


@Controller(name = "userController")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @ControllerAction(path = "addOrUpdateUser", isSynchronous = true)
    public String addOrUpdateUser(@ActionParam(name = "user")User user){
        log.debug("addOrUpdateUser user: " + user.getEmail());
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        Date now = new Date();
        String newUserId = ControllerUtils.generateGuid();

        //TODO - detail with partial updates.
        ITableRowUpdater tableUpdater = row -> {
            if(user.getUserId() == null){
                row.setString("userId", newUserId);
                row.setLong("created", now.getTime());
            }
            row.setLong("lastModified", now.getTime());
            row.setString("firstName", user.getFirstName());
            row.setString("lastName", user.getLastName());
            row.setString("password", ControllerUtils.encryptPassword(user.getPassword()));
            row.setString("contactNo", user.getContactNo());
            row.setString("email", user.getEmail());
            row.setString("type", user.getType());
            row.setString("stripeCustomerId", user.getStripeCustomerId());
            row.setString("stripeDefaultSourceId", user.getStripeDefaultSourceId());
        };

        if(user.getUserId() != null){
            userTable.updateRow(new TableKey(user.getUserId()), tableUpdater);
            log.debug("Updated user: " + user.getEmail() + " with id " + user.getUserId());
            return user.getUserId();
        }else{
            userTable.addRow(new TableKey(newUserId), tableUpdater);
            log.debug("Added user: " + user.getEmail() + " with id " + newUserId);
            return newUserId;
        }
    }

}
