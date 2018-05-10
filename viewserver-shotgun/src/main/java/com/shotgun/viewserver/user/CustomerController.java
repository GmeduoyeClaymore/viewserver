package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.messaging.AppMessage;
import com.shotgun.viewserver.messaging.AppMessageBuilder;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.shotgun.viewserver.ControllerUtils.getUserId;


@Controller(name = "customerController")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);


    private IPaymentController paymentController;
    private DeliveryAddressController deliveryAddressController;
    private IMessagingController messagingController;
    private UserController userController;
    private INexmoController nexmoController;

    public CustomerController(IPaymentController paymentController,
                              DeliveryAddressController deliveryAddressController,
                              IMessagingController messagingController,
                              UserController userController,
                              INexmoController nexmoController) {

        this.paymentController = paymentController;
        this.deliveryAddressController = deliveryAddressController;
        this.messagingController = messagingController;
        this.userController = userController;
        this.nexmoController = nexmoController;
    }

    @ControllerAction(path = "registerCustomer", isSynchronous = true)
    public String registerCustomer(@ActionParam(name = "user")User user, @ActionParam(name = "deliveryAddress")DeliveryAddress deliveryAddress){
        log.debug("Registering customer: " + user.getEmail());

        String international_format_number = nexmoController.getInternationalFormatNumber(user.getContactNo());
        if(international_format_number == null){
            throw new RuntimeException("Unable to format user contact number " + user.getContactNo() + " is it valid?");
        }
        user.set("contactNo",international_format_number);

        String userId = userController.addOrUpdateUser(user, user.getPassword());
        ControllerContext.set("userId", userId);

        deliveryAddress.set("isDefault",true);
        deliveryAddressController.addOrUpdateDeliveryAddress(deliveryAddress);

        log.debug("Registered customer: " + user.getEmail() + " with id " + userId);
        return userId;
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
