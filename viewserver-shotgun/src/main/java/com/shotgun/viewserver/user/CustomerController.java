package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.servercomponents.IDatabaseUpdater;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;

import static com.shotgun.viewserver.ControllerUtils.getUserId;


@Controller(name = "customerController")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private IDatabaseUpdater iDatabaseUpdater;
    private PaymentController paymentController;
    private DeliveryAddressController deliveryAddressController;
    private IMessagingController messagingController;
    private UserController userController;
    private INexmoController nexmoController;

    public CustomerController(IDatabaseUpdater iDatabaseUpdater,
                              PaymentController paymentController,
                              DeliveryAddressController deliveryAddressController,
                              IMessagingController messagingController,
                              UserController userController,
                              INexmoController nexmoController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.paymentController = paymentController;
        this.deliveryAddressController = deliveryAddressController;
        this.messagingController = messagingController;
        this.userController = userController;
        this.nexmoController = nexmoController;
    }

    @ControllerAction(path = "registerCustomer", isSynchronous = true)
    public String registerCustomer(@ActionParam(name = "user")User user, @ActionParam(name = "deliveryAddress")DeliveryAddress deliveryAddress, @ActionParam(name = "paymentCard")PaymentCard paymentCard){
        log.debug("Registering customer: " + user.getEmail());
        deliveryAddress.setIsDefault(true);
        HashMap<String, Object> stripeResponse = paymentController.createPaymentCustomer(user.getEmail(), paymentCard);
        user.setStripeCustomerId(stripeResponse.get("customerId").toString());
        user.setStripeDefaultSourceId(stripeResponse.get("paymentToken").toString());

        String international_format_number = (String) nexmoController.getInternationalFormatNumber(user.getContactNo());
        if(international_format_number == null){
            throw new RuntimeException("Unable to format user contact number " + user.getContactNo() + " is it valid?");
        }
        user.setContactNo(international_format_number);

        String userId = userController.addOrUpdateUser(user);
        ControllerContext.set("userId", userId);
        deliveryAddressController.addOrUpdateDeliveryAddress(deliveryAddress);

        log.debug("Registered customer: " + user.getEmail() + " with id " + userId);
        return userId;
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    public String cancelOrder(String orderId){
        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.CANCELLED.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);
        rejectDriver(orderId);
        return orderId;
    }

    @ControllerAction(path = "customerCompleteOrder", isSynchronous = true)
    public String customerCompleteOrder(String orderId){
        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.COMPLETEDBYCUSTOMER.name());
        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);
        String deliveryId = (String)ControllerUtils.getColumnValue(orderTable, "deliveryId", orderId);
        String driverId = (String)ControllerUtils.getColumnValue(deliveryTable, "driverId", deliveryId);
        notifyStatusChanged(orderId, driverId, "completed");
        return orderId;
    }


    @ControllerAction(path = "updateOrderPrice", isSynchronous = true)
    public String updateOrderPrice(@ActionParam(name = "orderId")String orderId,@ActionParam(name = "price")Double price){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String currentStatus = ControllerUtils.getColumnValue(orderTable, "status", currentRow).toString();
        String userId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        if(!userId.equals(getUserId())){
            throw new RuntimeException("You can only update the price of your own order");
        }
        Date now = new Date();

        IRecord orderRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("totalPrice", price)
                .addValue("lastModified", now);

        if(currentStatus != OrderStatuses.PLACED.name() && currentStatus != OrderStatuses.ACCEPTED.name() ){
            throw new RuntimeException("Price can only be updated if the order has not yet been started");
        }

        iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);
        return orderId;

    }

    @ControllerAction(path = "rejectDriver", isSynchronous = true)
    public String rejectDriver(String orderId){
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        String deliveryId = (String)ControllerUtils.getColumnValue(orderTable, "deliveryId", orderId);
        String driverId = (String)ControllerUtils.getColumnValue(deliveryTable, "driverId", deliveryId);

        if(driverId != null && driverId != "") {
            IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.PLACED.name());
            iDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

            IRecord deliveryRecord = new Record().addValue("deliveryId", deliveryId).addValue("driverId", "");
            iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);

            notifyStatusChanged(orderId, driverId, "cancelled");
        }

        return orderId;
    }

    private void notifyStatusChanged(String orderId, String orderDriverId, String status) {
        try {
            String formattedStatus = status.toLowerCase();
            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId, status))
                    .withFromTo(getUserId(),orderDriverId)
                    .message(String.format("Shotgun order %s", formattedStatus), String.format("Shotgun order has been %s by the customer", formattedStatus)).build();
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

        }catch (Exception ex){
            log.error("There was a problem sending the notification", ex);
        }
    }

    private String createActionUri(String orderId, String status){
        switch (status) {
            case "INPROGRESS":
                return String.format("shotgun://DriverOrderInProgress/%s", orderId);
            default:
                return String.format("shotgun://DriverOrderDetail/%s", orderId);
        }
    }


}
