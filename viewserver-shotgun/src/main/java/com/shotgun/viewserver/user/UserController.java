package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.TableUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller(name = "userController")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private LoginController loginController;
    private ImageController imageController;
    private NexmoController nexmoController;
    private TableUpdater tableUpdater;

    public UserController(TableUpdater tableUpdater,
                          LoginController loginController,
                          ImageController imageController,
                          NexmoController nexmoController) {
        this.tableUpdater = tableUpdater;

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
                .addValue("password", ControllerUtils.encryptPassword(user.getPassword()))
                .addValue("contactNo", nexmoController.getPhoneNumberInfo(user.getContactNo()).get("international_format_number"))
                .addValue("email", user.getEmail().toLowerCase())
                .addValue("type", user.getType())
                .addValue("stripeCustomerId", user.getStripeCustomerId())
                .addValue("stripeDefaultSourceId", user.getStripeDefaultSourceId())
                .addValue("stripeAccountId", user.getStripeAccountId())
                .addValue("imageUrl", user.getImageUrl());

        tableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
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

        tableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);

        log.debug("Updated user: " + user.getEmail() + " with id " + userId);
        return userId;
    }

    @ControllerAction(path = "setLocation", isSynchronous = true)
    public void setLocation(@ActionParam(name = "latitude") double latitude, @ActionParam(name = "longitude") double longitude) {
        String userId = getUserId();

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("latitude", latitude)
                .addValue("longitude", longitude);

        tableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
    }

    @ControllerAction(path = "updateStatus", isSynchronous = true)
    public void updateStatus(@ActionParam(name = "status", required = true) UserStatus status, @ActionParam(name = "statusMessage") String statusMessage) {
        String userId = getUserId();

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("status", status.name())
                .addValue("statusMessage", statusMessage);

        tableUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
    }

    public static rx.Observable<Map<String,Object>> waitForUser(String userId, KeyedTable userTable){
        int userRowId = userTable.getRow(new TableKey(userId));
        IOutput output = userTable.getOutput();
        if (userRowId == -1) {
            return output.observable().filter(ev -> hasUserId(ev,userId)).take(1).timeout(5, TimeUnit.SECONDS, Observable.error(UserNotFoundException.fromUserId(userId))).map(ev -> (Map<String,Object>)ev.getEventData());
        }
        return rx.Observable.just(OperatorEvent.getRowDetails(output,userRowId, null));
    }

    private static boolean hasUserId(OperatorEvent ev, String userId) {
        if(!ev.getEventType().equals(EventType.ROW_ADD)){
            return false;
        }
        if(ev.getEventData() == null){
            return false;
        }
        HashMap<String,Object> result = (HashMap<String,Object>)ev.getEventData();
        return userId.equals(result.get("userId"));
    }

    private String getUserId() {
        String userId = (String) ControllerContext.get("userId");
        if (userId == null) {
            throw new RuntimeException("Cannot find user id in controller context. Either you aren't logged in or you're doing this on a strange thread");
        }
        return userId;
    }

}
