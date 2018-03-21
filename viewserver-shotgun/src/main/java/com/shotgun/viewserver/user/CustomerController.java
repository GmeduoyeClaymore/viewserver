package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.FirebaseDatabaseUpdater;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.PaymentController;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


@Controller(name = "customerController")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private FirebaseDatabaseUpdater firebaseDatabaseUpdater;
    private PaymentController paymentController;
    private DeliveryAddressController deliveryAddressController;
    private MessagingController messagingController;
    private UserController userController;
    private NexmoController nexmoController;


    public CustomerController(FirebaseDatabaseUpdater firebaseDatabaseUpdater,
                              PaymentController paymentController,
                              DeliveryAddressController deliveryAddressController,
                              MessagingController messagingController,
                              UserController userController,
                              NexmoController nexmoController) {
        this.firebaseDatabaseUpdater = firebaseDatabaseUpdater;
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

        user.setContactNo((String)nexmoController.getPhoneNumberInfo(user.getContactNo()).get("international_format_number"));

        String userId = userController.addOrUpdateUser(user);
        ControllerContext.set("userId", userId);
        deliveryAddressController.addOrUpdateDeliveryAddress(deliveryAddress);

        log.debug("Registered customer: " + user.getEmail() + " with id " + userId);
        return userId;
    }

    @ControllerAction(path = "cancelOrder", isSynchronous = true)
    public String cancelOrder(String orderId){
        IRecord orderRecord = new Record().addValue("orderId", orderId).addValue("status", OrderStatuses.CANCELLED.name());
        firebaseDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

        rejectDriver(orderId);
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
            firebaseDatabaseUpdater.addOrUpdateRow(TableNames.ORDER_TABLE_NAME, "order", orderRecord);

            IRecord deliveryRecord = new Record().addValue("deliveryId", deliveryId).addValue("driverId", "");
            firebaseDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);

            notifyStatusChanged(orderId, driverId, "cancelled");
        }

        return orderId;
    }

    private void notifyStatusChanged(String orderId, String orderDriverId, String status) {
        try {
            String formattedStatus = status.toLowerCase();
            AppMessage builder = new AppMessageBuilder().withDefaults()
                    .withAction(createActionUri(orderId, status))
                    .message(String.format("Shotgun order %s", formattedStatus), String.format("Shotgun order has been %s by the customer", formattedStatus)).build();
            ListenableFuture future = messagingController.sendMessageToUser(orderDriverId, builder);

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
            case "PICKEDUP":
                return String.format("shotgun://DriverOrderInProgress/%s", orderId);
            default:
                return String.format("shotgun://DriverOrderDetail/%s", orderId);
        }
    }
}
