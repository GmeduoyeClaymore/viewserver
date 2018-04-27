package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.payments.PaymentBankAccount;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.PaymentController;
import com.stripe.model.Customer;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.IOperator;
import io.viewserver.operators.table.*;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.shotgun.viewserver.ControllerUtils.getUserId;

@Controller(name = "driverController")
public class DriverController {
    private static final Logger log = LoggerFactory.getLogger(DriverController.class);
    private IDatabaseUpdater iDatabaseUpdater;
    private PaymentController paymentController;
    private MessagingController messagingController;
    private UserController userController;
    private VehicleController vehicleController;
    private JourneyEmulatorController journeyEmulatorController;
    private DeliveryAddressController deliveryAddressController;
    private LoginController loginController;
    private ImageController imageController;
    private NexmoController nexmoController;
    private IReactor reactor;
    private boolean isMock;

    public DriverController(IDatabaseUpdater iDatabaseUpdater,
                            PaymentController paymentController,
                            MessagingController messagingController,
                            UserController userController,
                            VehicleController vehicleController,
                            JourneyEmulatorController journeyEmulatorController,
                            DeliveryAddressController deliveryAddressController,
                            LoginController loginController,
                            ImageController imageController,
                            NexmoController nexmoController,
                            IReactor reactor,
                            boolean isMock) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.paymentController = paymentController;
        this.messagingController = messagingController;
        this.userController = userController;
        this.vehicleController = vehicleController;
        this.deliveryAddressController = deliveryAddressController;
        this.loginController = loginController;
        this.journeyEmulatorController = journeyEmulatorController;
        this.imageController = imageController;
        this.nexmoController = nexmoController;
        this.reactor = reactor;
        this.isMock = isMock;
    }

    @ControllerAction(path = "registerDriver", isSynchronous = false)
    public ListenableFuture<String> registerDriver(@ActionParam(name = "user") User user,
                                                   @ActionParam(name = "vehicle") Vehicle vehicle,
                                                   @ActionParam(name = "address") DeliveryAddress address) {

        ITable userTable = ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        if (this.loginController.getUserRow(userTable, user.getEmail()) != -1) {
            throw new RuntimeException("Already  user registered for email " + user.getEmail());
        }

        log.debug("Registering driver: " + user.getEmail());
        //We can change this later on or on a per user basis
        user.setChargePercentage(10);
        user.setContactNo((String) nexmoController.getPhoneNumberInfo(user.getContactNo()).get("international_format_number"));

        SettableFuture<String> future = SettableFuture.create();
        ControllerContext context = ControllerContext.Current();
        reactor.scheduleTask(new ITask() {
            @Override
            public void execute() {
                try {
                    ControllerContext.create(context);
                    String userId = userController.addOrUpdateUser(user);
                    ControllerContext.set("userId", userId);
                    if (vehicle.getDimensions() != null) {
                        vehicleController.addOrUpdateVehicle(vehicle);
                    }
                    deliveryAddressController.addOrUpdateDeliveryAddress(address);

                    log.debug("Registered driver: " + user.getEmail() + " with id " + userId);
                    future.set(userId);
                } catch (Exception ex) {
                    log.error("There was a problem registering the driver", ex);
                    future.setException(ex);
                }
            }
        }, 0, 0);
        return future;
    }

    @ControllerAction(path = "acceptOrder", isSynchronous = true)
    public String acceptOrder(@ActionParam(name = "orderId") String orderId) {
        String driverId = getUserId();
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String currentStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();
        String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentRow).toString();

        if (currentStatus != OrderStatuses.PLACED.name()) {
            //TODO - handle this on the client side
            throw new RuntimeException("Order has already been assigned");
        }

        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.ACCEPTED.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        IRecord deliveryRecord = new Record().addValue("deliveryId", deliveryId).addValue("driverId", driverId);
        iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.ACCEPTED.name());
        return orderId;
    }

    @ControllerAction(path = "startOrder", isSynchronous = true)
    public String startOrder(@ActionParam(name = "orderId") String orderId) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        String driverId = getUserId();

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.PICKEDUP.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.PICKEDUP.name());

        if (isMock) {
            journeyEmulatorController.emulateJourneyForOrder(orderId, "emulator-5558", driverId);
        }

        return orderId;
    }

    @ControllerAction(path = "completeOrder", isSynchronous = true)
    public String completeOrder(@ActionParam(name = "orderId") String orderId) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        IOperator orderTableProjection = ControllerUtils.getOperator(TableNames.ORDER_TABLE_PROJECTION_OUTPUT_NAME);
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        String driverId = getUserId();
        int currentOrderRow = orderTable.getRow(new TableKey(orderId));
        int currentDriverRow = userTable.getRow(new TableKey(driverId));
        String orderUserId = (String) ControllerUtils.getColumnValue(orderTable, "userId", currentOrderRow);
        int currentCustomerRow = userTable.getRow(new TableKey(orderUserId));

        String paymentId = (String) ControllerUtils.getColumnValue(orderTable, "paymentId", currentOrderRow);
        String stripeCustomerId = (String) ControllerUtils.getColumnValue(userTable, "stripeCustomerId", currentCustomerRow);
        String accountId = (String) ControllerUtils.getColumnValue(userTable, "stripeAccountId", currentDriverRow);
        int chargePercentage = (int) ControllerUtils.getColumnValue(userTable, "chargePercentage", currentDriverRow);
        int totalPrice = (int) ControllerUtils.getColumnValue(orderTable, "totalPrice", currentOrderRow);

        String contentTypeName = (String) ControllerUtils.getOperatorColumnValue(orderTableProjection, "contentType_name", currentOrderRow);
        String productName = (String) ControllerUtils.getOperatorColumnValue(orderTableProjection, "product_name", currentOrderRow);
        String chargeDescription = String.format("%s (%s)", contentTypeName, productName);

        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.COMPLETED.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.COMPLETED.name());
        paymentController.createCharge(totalPrice, chargePercentage, paymentId, stripeCustomerId, accountId, chargeDescription);
        return orderId;
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    public String cancelOrder(@ActionParam(name = "orderId") String orderId) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        String driverId = getUserId();

        int currentOrderRow = orderTable.getRow(new TableKey(orderId));
        String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentOrderRow).toString();
        String orderUserId = (String) ControllerUtils.getColumnValue(orderTable, "userId", currentOrderRow);

        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.PLACED.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        IRecord deliveryRecord = new Record().addValue("deliveryId", deliveryId).addValue("driverId", "");
        iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);

        notifyStatusChanged(orderId, driverId, orderUserId, "cancelled");
        return orderId;
    }

    @ControllerAction(path = "setBankAccount", isSynchronous = false)
    public ListenableFuture<String> setBankAccount(@ActionParam(name = "paymentBankAccount") PaymentBankAccount paymentBankAccount, @ActionParam(name = "address") DeliveryAddress address) {
        try {
            User user = (User) ControllerContext.get("user");

            SettableFuture<String> future = SettableFuture.create();
            ControllerContext context = ControllerContext.Current();
            reactor.scheduleTask(new ITask() {
                @Override
                public void execute() {
                    ControllerContext.create(context);
                    String stripeAccountId = user.getStripeAccountId();
                    if (stripeAccountId == null) {
                        //no stripe account exists for this user, create it
                        stripeAccountId = paymentController.createPaymentAccount(user, address, paymentBankAccount);
                        user.setStripeAccountId(stripeAccountId);
                        userController.addOrUpdateUser(user);
                    } else {
                        paymentController.setBankAccount(paymentBankAccount);
                    }
                    future.set(stripeAccountId);
                    log.debug("Set bank account for stripe account {}", stripeAccountId);
                }
            }, 0, 0);
            return future;

        } catch (Exception e) {
            log.error("There was a problem setting the bank account", e);
            throw new RuntimeException(e);
        }
    }

    @ControllerAction(path = "addPaymentCard", isSynchronous = false)
    public ListenableFuture<String> addPaymentCard(@ActionParam(name = "paymentCard") PaymentCard paymentCard) {
        try {
            User user = (User) ControllerContext.get("user");

            SettableFuture<String> future = SettableFuture.create();
            ControllerContext context = ControllerContext.Current();
            reactor.scheduleTask(new ITask() {
                @Override
                public void execute() {
                    ControllerContext.create(context);
                    String customerToken = user.getStripeCustomerId();

                    if (customerToken == null) {
                        HashMap<String, Object> stripeResponse = paymentController.createPaymentCustomer(user.getEmail(), paymentCard);
                        user.setStripeCustomerId(stripeResponse.get("customerId").toString());
                        String defaultSourceId = stripeResponse.get("defaultSourceId").toString();
                        user.setStripeDefaultSourceId(defaultSourceId);
                        userController.addOrUpdateUser(user);
                        future.set(defaultSourceId);
                    } else {
                        String cardToken = paymentController.addPaymentCard(paymentCard);
                        future.set(cardToken);
                    }
                }
            }, 0, 0);
            return future;
        } catch (Exception e) {
            log.error("There was a problem adding the payment card", e);
            throw new RuntimeException(e);
        }
    }

    private void notifyStatusChanged(String orderId, String driverId, String orderUserId, String status) {
        try {
            KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
            int driverRow = userTable.getRow(new TableKey(driverId));
            String firstName = ControllerUtils.getColumnValue(userTable, "firstName", driverRow).toString();
            String lastName = ControllerUtils.getColumnValue(userTable, "lastName", driverRow).toString();
            String formattedStatus = status.toLowerCase();

            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId, status))
                    .message(String.format("Shotgun order %s", formattedStatus), String.format("%s has %s your Shotgun order", firstName + " " + lastName, formattedStatus))
                    .build();
            messagingController.sendMessageToUser(orderUserId, builder);
        } catch (Exception ex) {
            log.error("There was a problem sending the notification", ex);
        }
    }

    private String createActionUri(String orderId, String status) {
        switch (status) {
            case "PICKEDUP":
                return String.format("shotgun://CustomerOrderInProgress/%s", orderId);
            default:
                return String.format("shotgun://CustomerOrderDetail/%s", orderId);
        }
    }
}
