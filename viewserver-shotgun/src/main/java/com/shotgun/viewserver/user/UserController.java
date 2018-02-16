package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.ShotgunTableUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import io.viewserver.adapters.common.Record;
import io.viewserver.adapters.common.RowUpdater;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.datasource.DataSource;
import io.viewserver.datasource.IDataSourceRegistry;
import io.viewserver.datasource.LocalKeyedTableUpdater;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


@Controller(name = "userController")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private LoginController loginController;
    private ImageController imageController;
    private NexmoController nexmoController;
    private ShotgunTableUpdater shotgunTableUpdater;

    public UserController(ShotgunTableUpdater shotgunTableUpdater,
                          LoginController loginController,
                          ImageController imageController,
                          NexmoController nexmoController) {
        this.shotgunTableUpdater = shotgunTableUpdater;

        this.loginController = loginController;
        this.imageController = imageController;
        this.nexmoController = nexmoController;
    }

    @ControllerAction(path = "addOrUpdateUser", isSynchronous = true)
    public String addOrUpdateUser(@ActionParam(name = "user") User user) {
        log.debug("addOrUpdateUser user: " + user.getEmail());
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        if (this.loginController.getUserRow(userTable, user.getEmail()) != -1) {
            throw new RuntimeException("Already  user registered for email " + user.getEmail());
        }

        Date now = new Date();

        if (user.getUserId() == null) {
            user.setUserId(ControllerUtils.generateGuid());
            user.setCreated(now);
        }

        Record userRecord = new Record()
                .addValue("userId", user.getUserId())
                .addValue("lastModified", now)
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("selectedContentTypes", user.getSelectedContentTypes())
                .addValue("selectedProducts", user.getSelectedProducts())
                .addValue("password", ControllerUtils.encryptPassword(user.getPassword()))
                .addValue("contactNo", nexmoController.getPhoneNumberInfo(user.getContactNo()).get("international_format_number"))
                .addValue("email", user.getEmail().toLowerCase())
                .addValue("type", user.getType())
                .addValue("stripeCustomerId", user.getStripeCustomerId())
                .addValue("stripeDefaultSourceId", user.getStripeDefaultSourceId())
                .addValue("stripeAccountId", user.getStripeAccountId())
                .addValue("imageUrl", user.getImageUrl());

        shotgunTableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
        return user.getUserId();
    }


    @ControllerAction(path = "updateUser", isSynchronous = true)
    public String updateUser(@ActionParam(name = "user") User user) {
        log.debug("updateUser user: " + user.getEmail());
        String userId = getUserId();
        Date now = new Date();

        if (user.getImageData() != null) {
            String fileName = BucketNames.driverImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = imageController.saveToS3(BucketNames.shotgunclientimages.name(), fileName, user.getImageData());
            user.setImageUrl(imageUrl);
        }

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("lastModified", now)
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("contactNo", user.getContactNo())
                .addValue("email", user.getEmail().toLowerCase())
                .addValue("imageUrl", user.getImageUrl());

        shotgunTableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);

        log.debug("Updated user: " + user.getEmail() + " with id " + userId);
        return userId;
    }

    @ControllerAction(path = "setLocation", isSynchronous = true)
    public String setLocation(@ActionParam(name = "latitude") double latitude, @ActionParam(name = "longitude") double longitude) {
        String userId = getUserId();

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("latitude", latitude)
                .addValue("longitude", longitude);

        shotgunTableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);

        return userId;
    }

    private String getUserId() {
        String userId = (String) ControllerContext.get("userId");
        if (userId == null) {
            throw new RuntimeException("Cannot find user id in controller context. Either you aren't logged in or you're doing this on a strange thread");
        }
        return userId;
    }

}
