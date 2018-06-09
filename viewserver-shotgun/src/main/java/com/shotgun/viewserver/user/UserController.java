package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.constants.VanProducts;
import com.shotgun.viewserver.constants.VanVolumes;
import com.shotgun.viewserver.delivery.Dimensions;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.maps.LatLng;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.contracts.UserNotificationContract;
import com.shotgun.viewserver.order.controllers.contracts.RatedOrderController;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.payments.PaymentBankAccount;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.setup.datasource.UserDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.rx.EventType;
import io.viewserver.operators.rx.OperatorEvent;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import io.viewserver.schema.column.ColumnHolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observable.ListenableFutureObservable;
import rx.schedulers.Schedulers;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.shotgun.viewserver.ControllerUtils.getUserId;


@Controller(name = "userController")
public class UserController implements UserTransformationController, RatedOrderController, UserNotificationContract {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private IImageController IImageController;
    private IMessagingController messagingController;
    private IPaymentController paymentController;
    private IMapsController IMapsController;
    private IDatabaseUpdater iDatabaseUpdater;
    private IReactor reactor;
    private ICatalog catalog;

    public UserController(IDatabaseUpdater iDatabaseUpdater,
                          IImageController IImageController,
                          IMessagingController messagingController,
                          IPaymentController paymentController,
                          IMapsController IMapsController, IReactor reactor, ICatalog catalog) {
        this.iDatabaseUpdater = iDatabaseUpdater;

        this.IImageController = IImageController;
        this.messagingController = messagingController;
        this.paymentController = paymentController;
        this.IMapsController = IMapsController;
        this.reactor = reactor;
        this.catalog = catalog;
    }

    @ControllerAction(path = "addPaymentCard", isSynchronous = false)
    public ListenableFuture addPaymentCard(@ActionParam(name = "paymentCard") PaymentCard paymentCard) {
        AtomicReference<String> cardId = new AtomicReference<>();
        return ListenableFutureObservable.to(this.transformObservable(getUserId(),
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
                }, User.class).map( res -> cardId.get()));
    }

    public static List<String> getValidProductsVehicle(Dimensions dimensions) {
        log.debug(String.format("Getting valid products for vehicle with volume %s m cubed", dimensions.getVolume()));

        if (dimensions.getVolume() < VanVolumes.MediumVan) {
            log.debug("This is the volume of small van");
            return Arrays.asList(VanProducts.SmallVan);
        } else if (dimensions.getVolume() < VanVolumes.LargeVan) {
            log.debug("This is the volume of medium van");
            return Arrays.asList(VanProducts.SmallVan, VanProducts.MediumVan);
        } else if (dimensions.getVolume() < VanVolumes.Luton) {
            log.debug("This is the volume of large van");
            return Arrays.asList(VanProducts.SmallVan, VanProducts.MediumVan, VanProducts.LargeVan);
        } else {
            log.debug("This is the volume of luton");
            return Arrays.asList(VanProducts.SmallVan, VanProducts.MediumVan, VanProducts.LargeVan, VanProducts.Luton);
        }
    }

    @ControllerAction(path = "deletePaymentCard", isSynchronous = false)
    public ListenableFuture deletePaymentCard(@ActionParam(name = "cardId") String cardId) {
        return this.transform(getUserId(),
                user -> {
                    paymentController.deletePaymentCard(cardId);
                    user.deletePaymentCard(cardId);
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "setDefaultPaymentCard", isSynchronous = true)
    public ListenableFuture setDefaultPaymentCard(@ActionParam(name = "cardId") String cardId) {
        return this.transform(getUserId(),
                user -> {
                    user.setDefaultPaymentCard(cardId);
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "setBankAccount", isSynchronous = true)
    public ListenableFuture setBankAccount(@ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount, @ActionParam(name = "address") DeliveryAddress address) {
        return this.transform(getUserId(),
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
    public ListenableFuture updateUser(@ActionParam(name = "user") User user) {
        log.debug("updateUser user: " + user.getEmail());
        String userId = getUserId();
        user.set("userId", userId);

        if (user.getImageData() != null) {
            String fileName = BucketNames.driverImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = IImageController.saveImage(BucketNames.shotgunclientimages.name(), fileName, user.getImageData());
            user.set("imageUrl", imageUrl);
        }
        return this.addOrUpdateUser(user, null);
    }

    @ControllerAction(path = "setLocation", isSynchronous = false)
    public ListenableFuture setLocation(@ActionParam(name = "latitude") double latitude, @ActionParam(name = "longitude") double longitude) {
        return this.transform(getUserId(),
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
        reactor.scheduleTask(new ITask() {
            @Override
            public void execute() {
                try {
                    iDatabaseUpdater.addOrUpdateRow(TableNames.USER_TABLE_NAME, UserDataSource.getDataSource().getSchema(), userRecord, IRecord.UPDATE_LATEST_VERSION).subscribe(res -> future.set(result), err -> future.setException(err));
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
    public ListenableFuture updateStatus(@ActionParam(name = "status") UserStatus status, @ActionParam(name = "statusMessage") String statusMessage) {
        String userId = getUserId();


        return this.transform(getUserId(),
                user -> {
                    user.set("statusMessage", statusMessage);
                    user.set("userStatus", status.name());
                    return true;
                }, User.class);
    }

    @ControllerAction(path = "updateRelationship", isSynchronous = true)
    public ListenableFuture updateRelationship(@ActionParam(name = "targetUserId") String targetUserId, @ActionParam(name = "relationshipStatus") UserRelationshipStatus userRelationshipStatus, @ActionParam(name = "relationshipType") UserRelationshipType relationshipType) {
        String userId = getUserId();
        return this.transformAsync(
                targetUserId,
                targetUser -> {
                    targetUser.addOrUpdateRelationship(userId, getRelationshipStatus(userRelationshipStatus),relationshipType);
                    return getUserForId(userId,User.class).flatMap(
                            meUser -> {
                                Optional<UserRelationship> relationship = meUser.findUserRelationship(targetUserId);
                                if (relationship.isPresent() && relationship.get().getRelationshipStatus().equals(UserRelationshipStatus.BLOCKEDBYME)) {
                                } else {
                                    meUser.addOrUpdateRelationship(targetUserId, userRelationshipStatus, relationshipType);
                                }
                                return addOrUpdateUserObservable(meUser, null).map(res -> true);
                            }
                    );
                },
                user -> {
                    notifyRelationshipStatus(userId, targetUserId, userRelationshipStatus == null ? null : userRelationshipStatus.name());
                }, User.class
        );
    }

    private UserRelationshipStatus getRelationshipStatus(UserRelationshipStatus userRelationshipStatus) {
        if(userRelationshipStatus.equals(UserRelationshipStatus.REQUESTED)){
            return UserRelationshipStatus.REQUESTEDBYME;
        }
        if(userRelationshipStatus.equals(UserRelationshipStatus.BLOCKED)){
            return UserRelationshipStatus.BLOCKEDBYME;
        }
        return userRelationshipStatus;
    }




    private String getUsername(String userId, KeyedTable userTable) {
        String firstName = (String) ColumnHolderUtils.getColumnValue(userTable, "firstName", userId);
        String lastName = (String) ColumnHolderUtils.getColumnValue(userTable, "lastName", userId);
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

    @Override
    public KeyedTable getUserTable() {
        return (KeyedTable) catalog.getOperatorByPath(TableNames.USER_TABLE_NAME);
    }


    public static rx.Observable<Map<String, Object>> waitForUser(final String userId, KeyedTable userTable) {

        if (userId == null) {
            throw new RuntimeException("Userid must be specified");
        }
        int userRowId = userTable.getRow(new TableKey(userId));
        IOutput output = userTable.getOutput();
        if (userRowId == -1) {
            log.info("Waiting for user {}", userId);
            return output.observable().subscribeOn(Schedulers.from(ControllerUtils.BackgroundExecutor)).filter(ev -> hasUserId(ev, userId)).take(1).timeout(10, TimeUnit.SECONDS, Observable.error(UserNotFoundException.fromUserId(userId))).map(ev -> (Map<String, Object>) ev.getEventData());
        }
        return rx.Observable.just(OperatorEvent.getRowDetails(output, userRowId, null));
    }

    private static boolean hasUserId(OperatorEvent ev, String userId) {
        if (!ev.getEventType().equals(EventType.ROW_ADD) && !ev.getEventType().equals(EventType.ROW_UPDATE)) {
            return false;
        }
        if (ev.getEventData() == null) {
            return false;
        }
        HashMap<String, Object> result = (HashMap<String, Object>) ev.getEventData();
        return userId.equals(result.get("userId"));
    }

    @Override
    public ICatalog getSystemCatalog() {
        return catalog;
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
