package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


@Controller(name = "userController")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private LoginController loginController;
    private ImageController imageController;

    public UserController(LoginController loginController, ImageController imageController) {

        this.loginController = loginController;
        this.imageController = imageController;
    }

    @ControllerAction(path = "addOrUpdateUser", isSynchronous = true)
    public String addOrUpdateUser(@ActionParam(name = "user")User user){
        log.debug("addOrUpdateUser user: " + user.getEmail());
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        if(this.loginController.getUserRow(userTable,user.getEmail()) != -1){
            throw new RuntimeException("Already  user registered for email " + user.getEmail());
        }
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
            row.setString("selectedContentTypes", user.getSelectedContentTypes());
            row.setString("password", ControllerUtils.encryptPassword(user.getPassword()));
            row.setString("contactNo", user.getContactNo());
            row.setString("email", user.getEmail().toLowerCase());
            row.setString("type", user.getType());
            row.setString("stripeCustomerId", user.getStripeCustomerId());
            row.setString("stripeDefaultSourceId", user.getStripeDefaultSourceId());
            row.setString("imageUrl", user.getImageUrl());
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


    @ControllerAction(path = "updateUser", isSynchronous = true)
    public String updateUser(@ActionParam(name = "user")User user){
        log.debug("updateUser user: " + user.getEmail());
        String userId = getUserId();
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        Date now = new Date();

        if(user.getImageData() != null){
            String fileName = BucketNames.driverImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = imageController.saveToS3(BucketNames.shotgunclientimages.name(), fileName, user.getImageData());
            user.setImageUrl(imageUrl);
        }

        ITableRowUpdater tableUpdater = row -> {
            row.setLong("lastModified", now.getTime());
            row.setString("firstName", user.getFirstName());
            row.setString("lastName", user.getLastName());
            row.setString("contactNo", user.getContactNo());
            row.setString("email", user.getEmail().toLowerCase());
            row.setString("imageUrl", user.getImageUrl());
        };

        userTable.updateRow(new TableKey(userId), tableUpdater);
        log.debug("Updated user: " + user.getEmail() + " with id " + userId);
        return userId;
    }

    @ControllerAction(path = "setLocation", isSynchronous = true)
    public String setLocation(@ActionParam(name = "latitude")double latitude, @ActionParam(name = "longitude")double longitude){
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        String userId = getUserId();

        ITableRowUpdater tableUpdater = row -> {
            row.setDouble("latitude", latitude);
            row.setDouble("longitude", longitude);
        };

        userTable.updateRow(new TableKey(userId), tableUpdater);
        return userId;
    }

    private String getUserId() {
        String userId = (String) ControllerContext.get("userId");
        if(userId == null){
            throw new RuntimeException("Cannot find user id in controller context. Either you aren't logged in or you're doing this on a strange thread");
        }
        return userId;
    }

}
