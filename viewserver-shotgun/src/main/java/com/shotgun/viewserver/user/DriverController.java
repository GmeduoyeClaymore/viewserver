package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.servercomponents.IDatabaseUpdater;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.payments.PaymentBankAccount;
import com.shotgun.viewserver.payments.PaymentController;
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

import static com.shotgun.viewserver.ControllerUtils.getUserId;


@Controller(name = "driverController")
public class DriverController {
    private static final Logger log = LoggerFactory.getLogger(DriverController.class);
    private IDatabaseUpdater iDatabaseUpdater;
    private PaymentController paymentController;
    private IMessagingController messagingController;
    private UserController userController;
    private VehicleController vehicleController;
    private LoginController loginController;
    private IImageController IImageController;
    private INexmoController nexmoController;
    private IReactor reactor;

    public DriverController(IDatabaseUpdater iDatabaseUpdater,
                            PaymentController paymentController,
                            IMessagingController messagingController,
                            UserController userController,
                            VehicleController vehicleController,
                            LoginController loginController,
                            IImageController IImageController,
                            INexmoController nexmoController,
                            IReactor reactor) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.paymentController = paymentController;
        this.messagingController = messagingController;
        this.userController = userController;
        this.vehicleController = vehicleController;
        this.loginController = loginController;
        this.IImageController = IImageController;
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
            String imageUrl = IImageController.saveImage(BucketNames.shotgunclientimages.name(), fileName, user.getImageData());
            user.setImageUrl(imageUrl);
        }

        user.setContactNo((String)nexmoController.getInternationalFormatNumber(user.getContactNo()));

        SettableFuture<String> future = SettableFuture.create();
        ControllerContext context = ControllerContext.Current();
        reactor.scheduleTask(new ITask() {
            @Override
            public void execute() {
                try{
                    ControllerContext.create(context);
                    user.setStripeAccountId(paymentAccountId);
                    String userId = userController.addOrUpdateUser(user);
                    ControllerContext.set("userId", userId);
                    if(vehicle.getDimensions() != null) {
                        vehicleController.addOrUpdateVehicle(vehicle);
                    }
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

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String currentStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();
        String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentRow).toString();

        if(currentStatus != OrderStatuses.PLACED.name()){
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
    public String startOrder(@ActionParam(name = "orderId")String orderId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        String driverId = getUserId();

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.INPROGRESS.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.INPROGRESS.name());


        return orderId;
    }

    @ControllerAction(path = "completeOrder", isSynchronous = true)
    public String completeOrder(@ActionParam(name = "orderId")String orderId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        IOperator orderTableProjection = ControllerUtils.getOperator(TableNames.ORDER_TABLE_PROJECTION_OUTPUT_NAME);
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
        int totalPrice = (int)ControllerUtils.getColumnValue(orderTable, "totalPrice", currentOrderRow);

        String contentTypeName = (String)ControllerUtils.getOperatorColumnValue(orderTableProjection, "contentType_name", currentOrderRow);
        String productName = (String)ControllerUtils.getOperatorColumnValue(orderTableProjection, "product_name", currentOrderRow);
        String chargeDescription = String.format("%s (%s)", contentTypeName, productName);

        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.COMPLETED.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        notifyStatusChanged(orderId, driverId, orderUserId, OrderStatuses.COMPLETED.name());
        paymentController.createCharge(totalPrice, chargePercentage, paymentId, stripeCustomerId, accountId, chargeDescription);
        return orderId;
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    public String cancelOrder(@ActionParam(name = "orderId")String orderId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        String driverId = getUserId();

        int currentOrderRow = orderTable.getRow(new TableKey(orderId));
        String deliveryId = ControllerUtils.getColumnValue(orderTable, "deliveryId", currentOrderRow).toString();
        String orderUserId = (String)ControllerUtils.getColumnValue(orderTable, "userId", currentOrderRow);

        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.PLACED.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        IRecord deliveryRecord = new Record().addValue("deliveryId", deliveryId).addValue("driverId", "");
        iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);

        notifyStatusChanged(orderId, driverId, orderUserId, "cancelled");
        return orderId;
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
                    .withFromTo(driverId,orderUserId)
                    .message(String.format("Shotgun order %s", formattedStatus), String.format("%s has %s your Shotgun order", firstName + " " + lastName, formattedStatus))
                    .build();
            messagingController.sendMessageToUser(builder);
        }catch (Exception ex){
            log.error("There was a problem sending the notification", ex);
        }
    }

    private String createActionUri(String orderId, String status){
        switch (status) {
            case "INPROGRESS":
                return String.format("shotgun://CustomerOrderInProgress/%s", orderId);
            default:
                return String.format("shotgun://CustomerOrderDetail/%s", orderId);
        }
    }
}
