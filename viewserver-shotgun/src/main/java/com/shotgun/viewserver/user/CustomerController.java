package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.OrderStatuses;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


@Controller(name = "customerController")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private PaymentController paymentController;
    private DeliveryAddressController deliveryAddressController;
    private MessagingController messagingController;
    private UserController userController;
    private NexmoController nexmoController;


    public CustomerController(PaymentController paymentController,
                              DeliveryAddressController deliveryAddressController,
                              MessagingController messagingController,
                              UserController userController,
                              NexmoController nexmoController) {
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
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        orderTable.updateRow(new TableKey(orderId), row -> {
            row.setString("status", OrderStatuses.CANCELLED.name());
        });

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
            orderTable.updateRow(new TableKey(orderId), row -> {
                row.setString("status", OrderStatuses.PLACED.name());
            });

            deliveryTable.updateRow(new TableKey(deliveryId), row -> {
                row.setString("driverId", null);
            });

            notifyStatusChanged(orderId, driverId, "cancelled");
        }

        return orderId;
    }

    private void notifyStatusChanged(String orderId, String orderDriverId, String status) {
        try {
            String formattedStatus = status.toLowerCase();
            AppMessage builder = new AppMessageBuilder().withDefaults().withData("orderId", orderId)
                    .message(String.format("Shotgun order %s", formattedStatus), String.format("Shotgun order has been %s by the customer", formattedStatus)).build();
            messagingController.sendMessageToUser(orderDriverId, builder);
        }catch (Exception ex){
            log.error("There was a problem sending the notification", ex);
        }
    }
}
