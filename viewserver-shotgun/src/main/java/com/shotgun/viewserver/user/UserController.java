package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.controllers.contracts.RatedOrderController;
import com.shotgun.viewserver.payments.PaymentBankAccount;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.maps.LatLng;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import com.shotgun.viewserver.setup.datasource.UserRelationshipDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
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
import java.util.concurrent.atomic.AtomicReference;

import static com.shotgun.viewserver.ControllerUtils.getUserId;


@Controller(name = "userController")
public class UserController implements UserTransformationController, RatedOrderController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private IImageController IImageController;
    private IMessagingController messagingController;
    private IPaymentController paymentController;
    private IMapsController IMapsController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IReactor reactor;

    public UserController(IDatabaseUpdater iDatabaseUpdater,
                          IImageController IImageController,
                          IMessagingController messagingController,
                          IPaymentController paymentController,
                          IMapsController IMapsController, IReactor reactor) {
        this.iDatabaseUpdater = iDatabaseUpdater;

        this.IImageController = IImageController;
        this.messagingController = messagingController;
        this.paymentController = paymentController;
        this.IMapsController = IMapsController;
        this.reactor = reactor;
    }

    @ControllerAction(path = "addOrUpdateRating", isSynchronous = true)
    public void addOrUpdateRating(@ActionParam(name = "userId") String userId, @ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating, @ActionParam(name = "comments") String comments, @ActionParam(name = "ratingType") UserRating.RatingType ratingType) {
        this.transform(userId,
                user -> {
                    user.addRating(getUserId(), orderId, rating, comments, ratingType);
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "addPaymentCard", isSynchronous = false)
    public String addPaymentCard(@ActionParam(name = "paymentCard") PaymentCard paymentCard) {
        AtomicReference<String> cardId = new AtomicReference<>();

        this.transform(getUserId(),
                user -> {
                    String customerToken = user.getStripeCustomerId();
                    SavedPaymentCard savedPaymentCard;
                    if (customerToken == null) {
                        HashMap<String, Object> stripeResponse = paymentController.createPaymentCustomer(user.getEmail(), paymentCard);
                        user.set("stripeCustomerId", stripeResponse.get("stripeCustomerId").toString());
                        savedPaymentCard = (SavedPaymentCard) stripeResponse.get("savedPaymentCard");
                    } else {
                        savedPaymentCard = paymentController.addPaymentCard(paymentCard);
                    }

                    user.addPaymentCard(savedPaymentCard);
                    cardId.set(savedPaymentCard.getCardId());
                    return true;
                }, User.class);

        return cardId.get();
    }

    @ControllerAction(path = "deletePaymentCard", isSynchronous = false)
    public void deletePaymentCard(@ActionParam(name = "deletePaymentCard") String cardId) {
        this.transform(getUserId(),
                user -> {
                    paymentController.deletePaymentCard(cardId);
                    user.deletePaymentCard(cardId);
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "setDefaultPaymentCard", isSynchronous = true)
    public void setDefaultPaymentCard(@ActionParam(name = "setDefaultPaymentCard") String cardId) {
        this.transform(getUserId(),
                user -> {
                    user.setDefaultPaymentCard(cardId);
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "setBankAccount", isSynchronous = true)
    public void setBankAccount(@ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount, @ActionParam(name = "address") DeliveryAddress address) {
        this.transform(getUserId(),
                user -> {
                    SavedBankAccount savedBankAccount;
                    String stripeAccountId = user.getStripeAccountId();
                    if (stripeAccountId == null) {
                        //no stripe account exists for this user, create it
                        HashMap<String, Object> stripeResponse = paymentController.createPaymentAccount(user, address, paymentBankAccount);
                        user.set("stripeAccountId", stripeResponse.get("stripeAccountId").toString());
                        savedBankAccount = (SavedBankAccount) stripeResponse.get("savedBankAccount");
                    } else {
                        savedBankAccount = paymentController.setBankAccount(paymentBankAccount);
                    }

                    user.setBankAccount(savedBankAccount);
                    return true;
                }, User.class);
    }


    @ControllerAction(path = "updateUser", isSynchronous = true)
    public String updateUser(@ActionParam(name = "user") User user) {
        log.debug("updateUser user: " + user.getEmail());
        String userId = getUserId();
        user.set("userId", userId);

        if (user.getImageData() != null) {
            String fileName = BucketNames.driverImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = IImageController.saveImage(BucketNames.shotgunclientimages.name(), fileName, user.getImageData());
            user.set("imageUrl", imageUrl);
        }
        return this.addOrUpdateUser(user);
    }

    @ControllerAction(path = "setLocation", isSynchronous = true)
    public void setLocation(@ActionParam(name = "latitude") double latitude, @ActionParam(name = "longitude") double longitude) {
        this.transform(getUserId(),
                user -> {
                    user.set("latitude", latitude);
                    user.set("longitude", longitude);
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "setLocationFromPostcode", isSynchronous = false)
    public ListenableFuture setLocationFromPostcode(@ActionParam(name = "postcode") String postcode) {
        String userId = getUserId();
        LatLng result = IMapsController.getLocationFromPostcode(postcode);

        Record userRecord = new Record()
                .addValue("userId", userId)
                .addValue("latitude", result.getLatitude())
                .addValue("longitude", result.getLongitude());

        SettableFuture<LatLng> future = SettableFuture.create();
        KeyedTable table = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        reactor.scheduleTask(new ITask() {
            @Override
            public void execute() {
                try {
                    iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord);
                    future.set(result);
                } catch (Exception ex) {
                    log.error("There was a problem setting user location from postcode", ex);
                    future.setException(ex);
                }
            }
        }, 0, 0);
        return future;

    }

    @ControllerAction(path = "updateStatus", isSynchronous = true)
    public void updateStatus(@ActionParam(name = "status") UserStatus status, @ActionParam(name = "statusMessage") String statusMessage) {
        String userId = getUserId();


        this.transform(getUserId(),
                user -> {
                    user.set("statusMessage", statusMessage);
                    user.set("userStatus", status.name());
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "updateRelationship", isSynchronous = true)
    public void updateRelationship(@ActionParam(name = "targetUserId") String targetUserId, @ActionParam(name = "relationshipStatus") UserRelationshipStatus userRelationshipStatus, @ActionParam(name = "relationshipType") UserRelationshipType relationshipType) {
        String userId = getUserId();

        Record userRecord = new Record()
                .addValue("relationshipId", constructKey(userId, targetUserId))
                .addValue("fromUserId", userId)
                .addValue("toUserId", targetUserId)
                .addValue("relationshipStatus", userRelationshipStatus == null ? null : userRelationshipStatus.name())
                .addValue("relationshipType", relationshipType == null ? null : relationshipType.name());

        iDatabaseUpdater.addOrUpdateRow(TableNames.USER_RELATIONSHIP_TABLE_NAME, UserRelationshipDataSource.getDataSource().getSchema(), userRecord);

        notifyRelationshipStatus(userId, targetUserId, userRelationshipStatus == null ? null : userRelationshipStatus.name(), ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME));
    }

    private void notifyRelationshipStatus(String userId, String targetUserId, String status, KeyedTable userTable) {
        try {
            String formattedStatus = status.toLowerCase();
            String fromUserName = getUsername(userId, userTable);
            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createUserActionUri(userId))
                    .withFromTo(userId, targetUserId)
                    .message(String.format("Shotgun friend request", formattedStatus), String.format("Shotgun user %s has %s your friendship", fromUserName, formattedStatus)).build();
            ListenableFuture future = messagingController.sendMessageToUser(builder);

            Futures.addCallback(future, new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object o) {
                    log.debug("Message sent successfully");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    log.error("There was a problem sending the notification", throwable);
                }
            });

        } catch (Exception ex) {
            log.error("There was a problem sending the notification", ex);
        }
    }

    private String createUserActionUri(String targetUserId) {
        return String.format("shotgun://Landing/UserRelationships/RelationshipView/DetailX/SelectedUser%sX", targetUserId);
    }

    private String getUsername(String userId, KeyedTable userTable) {
        String firstName = (String) ControllerUtils.getColumnValue(userTable, "firstName", userId);
        String lastName = (String) ControllerUtils.getColumnValue(userTable, "lastName", userId);
        return firstName + " " + lastName;
    }

    private String constructKey(String userId, String targetUserId) {
        List<String> list = Arrays.asList(userId, targetUserId);
        Collections.sort(list);
        return String.join(">", list);
    }

    @ControllerAction(path = "updateRange", isSynchronous = true)
    public void updateRange(@ActionParam(name = "range") int range) {
        this.transform(getUserId(),
                user -> {
                    user.set("range", range);
                    return true;
                }, User.class);
    }

    public static rx.Observable<Map<String, Object>> waitForUser(final String userId, KeyedTable userTable) {

        if (userId == null) {
            throw new RuntimeException("Userid must be specified");
        }
        int userRowId = userTable.getRow(new TableKey(userId));
        IOutput output = userTable.getOutput();
        if (userRowId == -1) {
            log.info("Waiting for user {}", userId);
            return output.observable().filter(ev -> hasUserId(ev, userId)).take(1).timeout(30, TimeUnit.SECONDS, Observable.error(UserNotFoundException.fromUserId(userId))).map(ev -> (Map<String, Object>) ev.getEventData());
        }
        return rx.Observable.just(OperatorEvent.getRowDetails(output, userRowId, null));
    }

    private static boolean hasUserId(OperatorEvent ev, String userId) {
        if (!ev.getEventType().equals(EventType.ROW_ADD)) {
            return false;
        }
        if (ev.getEventData() == null) {
            return false;
        }
        HashMap<String, Object> result = (HashMap<String, Object>) ev.getEventData();
        return userId.equals(result.get("userId"));
    }

    @Override
    public IDatabaseUpdater getDatabaseUpdater() {
        return this.iDatabaseUpdater;
    }

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public IMessagingController getMessagingController() {
        return this.messagingController;
    }
}
