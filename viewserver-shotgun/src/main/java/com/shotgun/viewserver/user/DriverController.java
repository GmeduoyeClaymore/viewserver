package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.payments.PaymentBankAccount;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.*;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Future;


@Controller(name = "driverController")
public class DriverController {
    private static final Logger log = LoggerFactory.getLogger(DriverController.class);
    private PaymentController paymentController;
    private MessagingController messagingController;
    private UserController userController;
    private VehicleController vehicleController;
    private JourneyEmulatorController journeyEmulatorController;
    private LoginController loginController;
    private ImageController imageController;
    private NexmoController nexmoController;
    private IReactor reactor;

    public DriverController(PaymentController paymentController,
                            MessagingController messagingController,
                            UserController userController,
                            VehicleController vehicleController,
                            JourneyEmulatorController journeyEmulatorController,
                            LoginController loginController,
                            ImageController imageController,
                            NexmoController nexmoController,
                            IReactor reactor) {
        this.paymentController = paymentController;
        this.messagingController = messagingController;
        this.userController = userController;
        this.vehicleController = vehicleController;
        this.loginController = loginController;
        this.journeyEmulatorController = journeyEmulatorController;
        this.imageController = imageController;
        this.nexmoController = nexmoController;
        this.reactor = reactor;
    }

    @ControllerAction(path = "registerDriver", isSynchronous = false)
    public ListenableFuture<String> registerDriver(@ActionParam(name = "user")User user,
                                 @ActionParam(name = "vehicle")Vehicle vehicle,
                                 @ActionParam(name = "address")DeliveryAddress address,
                                 @ActionParam(name = "bankAccount")PaymentBankAccount bankAccount

    ){

        ITable userTable = ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        if(this.loginController.getUserRow(userTable,user.getEmail()) != -1){
            throw new RuntimeException("Already  user registered for email " + user.getEmail());
        }

        log.debug("Registering driver: " + user.getEmail());
        //We can change this later on or on a per user basis
        user.setChargePercentage(10);

        String paymentAccountId = paymentController.createPaymentAccount(user, address, bankAccount);

        //save image if required
        if(user.getImageData() != null){
            String fileName = BucketNames.driverImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = imageController.saveToS3(BucketNames.shotgunclientimages.name(), fileName, user.getImageData());
            user.setImageUrl(imageUrl);
        }

        user.setContactNo((String)nexmoController.getPhoneNumberInfo(user.getContactNo()).get("international_format_number"));
        user.setSelectedProducts(String.join(",", vehicleController.getValidProductsVehicle(vehicle)));

        SettableFuture<String> future = SettableFuture.create();
        ControllerContext context = ControllerContext.Current();
        reactor.scheduleTask(new ITask() {
            @Override
            public void execute() {
                try{
                    ControllerContext.create(context);
                    user.setStripeAccountId(paymentAccountId);
                    String userId = userController.addOrUpdateUser(user);
                    ControllerContext.set("userId",userId);
                    vehicleController.addOrUpdateVehicle(vehicle);
                    log.debug("Registered driver: " + user.getEmail() + " with id " + userId);
                    future.set(userId);
                }catch (Exception ex){
                    log.error("There was a problem registering the driver", ex);
                    future.setException(ex);
                }
            }
        },0,0);
        return future;
    }

    @ControllerAction(path = "acceptOrder", isSynchronous = true)
    public String acceptOrder(@ActionParam(name = "orderId")String orderId){
        String driverId = getUserId();
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String currentStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();
        String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentRow).toString();

        if(currentStatus != OrderStatuses.PLACED.name()){
            //TODO - handle this on the client side
            throw new RuntimeException("Order has already been assigned");
        }

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.ACCEPTED.name());
        });

        deliveryTable.updateRow(new TableKey(deliveryId), row -> {
            row.setString("driverId", driverId);
        });

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.ACCEPTED.name());
        return orderId;
    }


    @ControllerAction(path = "startOrder", isSynchronous = true)
    public String startOrder(@ActionParam(name = "orderId")String orderId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        String driverId = getUserId();

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.PICKEDUP.name());
        });

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.PICKEDUP.name());

        journeyEmulatorController.emulateJourneyForOrder(orderId, "emulator-5558", driverId);
        return orderId;
    }

    @ControllerAction(path = "completeOrder", isSynchronous = true)
    public String completeOrder(@ActionParam(name = "orderId")String orderId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable userTable = ControllerUtils.getKeyedTable(TableNames.USER_TABLE_NAME);
        String driverId = getUserId();
        int currentOrderRow = orderTable.getRow(new TableKey(orderId));
        int currentDriverRow = userTable.getRow(new TableKey(driverId));
        String orderUserId = (String)ControllerUtils.getColumnValue(orderTable, "userId", currentOrderRow);
        int currentCustomerRow = userTable.getRow(new TableKey(orderUserId));

        String paymentId = (String)ControllerUtils.getColumnValue(orderTable, "paymentId", currentOrderRow);
        String stripeCustomerId = (String)ControllerUtils.getColumnValue(userTable, "stripeCustomerId", currentCustomerRow);
        String accountId = (String)ControllerUtils.getColumnValue(userTable, "stripeAccountId", currentDriverRow);
        int chargePercentage = (int)ControllerUtils.getColumnValue(userTable, "chargePercentage", currentDriverRow);
        Double totalPrice = (Double)ControllerUtils.getColumnValue(orderTable, "totalPrice", currentOrderRow);

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.COMPLETED.name());
        });

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.COMPLETED.name());
        paymentController.createCharge(totalPrice, chargePercentage, paymentId, stripeCustomerId, accountId);
        return orderId;
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    public String cancelOrder(@ActionParam(name = "orderId")String orderId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);
        String driverId = getUserId();

        int currentOrderRow = orderTable.getRow(new TableKey(orderId));
        String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentOrderRow).toString();
        String orderUserId = (String)ControllerUtils.getColumnValue(orderTable, "userId", currentOrderRow);
        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.PLACED.name());
        });

        deliveryTable.updateRow(new TableKey(deliveryId), row -> {
            row.setString("driverId", null);
        });

        notifyStatusChanged(orderId, driverId, orderUserId, "cancelled");
        return orderId;
    }

    private String getUserId() {
        String driverId = (String) ControllerContext.get("userId");
        if(driverId == null){
            throw new RuntimeException("Cannot find user id in controller context. Either you aren't logged in or you're doing this on a strange thread");
        }
        return driverId;
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
        }catch (Exception ex){
            log.error("There was a problem sending the notification", ex);
        }
    }

    private String createActionUri(String orderId, String status){
        switch (status) {
            case "PICKEDUP":
                return String.format("shotgun://CustomerOrderInProgress/%s", orderId);
            default:
                return String.format("shotgun://CustomerOrderDetail/%s", orderId);
        }
    }
}
