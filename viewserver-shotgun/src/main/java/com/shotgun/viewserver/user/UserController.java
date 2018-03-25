package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.MapsController;
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
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.shotgun.viewserver.ControllerUtils.getUserId;


@Controller(name = "userController")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private LoginController loginController;
    private ImageController imageController;
    private NexmoController nexmoController;
    private MapsController mapsController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IReactor reactor;



    public UserController(IDatabaseUpdater iDatabaseUpdater,
                          LoginController loginController,
                          ImageController imageController,
                          NexmoController nexmoController,
                          MapsController mapsController, IReactor reactor) {
        this.iDatabaseUpdater = iDatabaseUpdater;

        this.loginController = loginController;
        this.imageController = imageController;
        this.nexmoController = nexmoController;
        this.mapsController = mapsController;
        this.reactor = reactor;

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
                .addValue("range", user.getRange())
                .addValue("stripeCustomerId", user.getStripeCustomerId())
                .addValue("stripeDefaultSourceId", user.getStripeDefaultSourceId())
                .addValue("stripeAccountId", user.getStripeAccountId())
                .addValue("imageUrl", user.getImageUrl());

        iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
        return user.getUserId();
    }


    @ControllerAction(path = "updateUser", isSynchronous = true)
    public String updateUser(@ActionParam(name = "user") User user) {
        log.debug("updateUser user: " + user.getEmail());
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);

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
                .addValue("selectedContentTypes", user.getSelectedContentTypes())
                .addValue("contactNo", user.getContactNo())
                .addValue("email", user.getEmail().toLowerCase())
                .addValue("imageUrl", user.getImageUrl());

        iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);

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

        iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
    }

    @ControllerAction(path = "setLocationFromPostcode", isSynchronous = false)
    public ListenableFuture setLocationFromPostcode(@ActionParam(name = "postcode") String postcode) {
        String userId = getUserId();
        HashMap<String, Object> result = mapsController.getLocationFromPostcode(postcode);

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("latitude", result.get("lat"))
                .addValue("longitude", result.get("lng"));

        SettableFuture<HashMap<String, Object>> future = SettableFuture.create();
        KeyedTable table = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        reactor.scheduleTask(new ITask() {
            @Override
            public void execute() {
                try{
                    iDatabaseUpdater.addOrUpdateRow(table, "user", userRecord);
                    future.set(result);
                }catch (Exception ex){
                    log.error("There was a problem setting user location from postcode", ex);
                    future.setException(ex);
                }
            }
        },0,0);
        return future;

    }


    @ControllerAction(path = "updateStatus", isSynchronous = true)
    public void updateStatus(@ActionParam(name = "status") UserStatus status, @ActionParam(name = "statusMessage") String statusMessage) {
        String userId = getUserId();

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("statusMessage", statusMessage);

        if(status != null){
            userRecord.addValue("status", status.name());

        }

        iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
    }

    @ControllerAction(path = "updateRelationship", isSynchronous = true)
    public void updateRelationship(@ActionParam(name = "targetUserId") String targetUserId, @ActionParam(name = "relationshipStatus") UserRelationshipStatus userRelationshipStatus, @ActionParam(name = "relationshipType") UserRelationshipType relationshipType) {
        String userId = getUserId();

        Record userRecord = new Record()
                .addValue("relationshipId", constructKey(userId,targetUserId))
                .addValue("fromUserId", userId)
                .addValue("toUserId", targetUserId)
                .addValue("relationshipStatus", userRelationshipStatus == null ? null : userRelationshipStatus.name())
                .addValue("relationshipType", relationshipType == null ? null : relationshipType.name());

        iDatabaseUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, "userRelationship", userRecord);
    }

    private String constructKey(String userId, String targetUserId) {
        List<String> list = Arrays.asList(userId, targetUserId);
        Collections.sort(list);
        return String.join(">",list);
    }


    @ControllerAction(path = "updateRange", isSynchronous = true)
    public void updateRange(@ActionParam(name = "range") int range) {
        String userId = getUserId();

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("range", range);

        iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, "user", userRecord);
    }

    public static rx.Observable<Map<String,Object>> waitForUser(final String userId, KeyedTable userTable){

        if(userId == null){
            throw new RuntimeException("Userid must be specified");
        }
        int userRowId = userTable.getRow(new TableKey(userId));
        IOutput output = userTable.getOutput();
        if (userRowId == -1) {
            log.info("Waiting for user {}",userId);
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

}
