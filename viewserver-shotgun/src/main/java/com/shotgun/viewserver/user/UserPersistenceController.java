package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import org.slf4j.Logger;
import rx.Observable;
import rx.observable.ListenableFutureObservable;
import rx.schedulers.Schedulers;

import java.util.Date;
import java.util.HashMap;

public interface UserPersistenceController{

    default <T extends User> Observable<T> getUserForId(String userId, Class<T> userClass) {
        KeyedTable userTable = getUserTable();
        ControllerContext context = ControllerContext.Current();
        return userTable.waitForRow(new TableKey(userId), Schedulers.from(ControllerUtils.BackgroundExecutor)).observeOn(ControllerContext.Scheduler(context)).map(
                currentRow -> {
                    if(currentRow == -1){
                        throw new RuntimeException("Unable to find user for key:" + userId);
                    }
                    HashMap<String, Object> userRow = userTable.getRowObject(new TableKey(userId));
                    if(userRow == null){
                        throw new RuntimeException(String.format("Unable to find user for id %s",userId));
                    }
                    return JSONBackedObjectFactory.create(userRow, userClass);
                }
        );

    }


    @ControllerAction(path = "addOrUpdateUser", isSynchronous = true)
    default ListenableFuture addOrUpdateUser(@ActionParam(name = "user") User user, @ActionParam(name = "password") String password) {
        return ListenableFutureObservable.to(addOrUpdateUserObservable(user,password));
    }
    default Observable<String> addOrUpdateUserObservable(@ActionParam(name = "user") User user, @ActionParam(name = "password") String password) {
        getLogger().debug("addOrUpdateUser user: " + user.getEmail());
        if(user.getUserId() == null) {
            user.set("userId", ControllerUtils.generateGuid());
        }
        Date now = new Date();
        Record userRecord = new Record()
                .addValue("userId", user.getUserId())
                .addValue("lastModified", now)
                .addValue("created", user.getCreated())
                .addValue("ratings", user.getRatings())
                .addValue("paymentCards", user.getPaymentCards())
                .addValue("bankAccount", user.getBankAccount())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("dob", user.getDob())
                .addValue("selectedContentTypes", user.getSelectedContentTypes())
                .addValue("relationships", user.getRelationships())
                .addValue("contactNo", user.getContactNo())
                .addValue("email", user.getEmail().toLowerCase())
                .addValue("type", user.getType())
                .addValue("vehicle", user.getVehicle())
                .addValue("latitude", user.getLatitude())
                .addValue("longitude", user.getLongitude())
                .addValue("online", user.getOnline())
                .addValue("userStatus", user.getUserStatus())
                .addValue("userAppStatus", user.getUserAppStatus())
                .addValue("pendingMessages", user.getPendingMessages())
                .addValue("blockedByUsers", user.getBlockedByUsers())
                .addValue("range", user.getRange())
                .addValue("ratingAvg", user.getRatingAvg())
                .addValue("stripeCustomerId", user.getStripeCustomerId())
                .addValue("stripeAccountId", user.getStripeAccountId())
                .addValue("imageUrl", user.getImageUrl())
                .addValue("version", user.getVersion())
                .addValue("chargePercentage", user.getChargePercentage());

        if(password != null){
            userRecord.addValue("password", ControllerUtils.encryptPassword(password));
        }

        return getDatabaseUpdater().addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord).map(res -> user.getUserId());
    }

    IDatabaseUpdater getDatabaseUpdater();

    KeyedTable getUserTable();

    Logger getLogger();
}
