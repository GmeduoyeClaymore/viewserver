package com.shotgun.viewserver.user;

import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


@Controller(name = "customerController")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private PaymentController paymentController;
    private DeliveryAddressController deliveryAddressController;


    public CustomerController(PaymentController paymentController, DeliveryAddressController deliveryAddressController) {
        this.paymentController = paymentController;
        this.deliveryAddressController = deliveryAddressController;
    }

    @ControllerAction(path = "registerCustomer", isSynchronous = true)
    public String registerCustomer(@ActionParam(name = "user")User user, @ActionParam(name = "deliveryAddress")DeliveryAddress deliveryAddress, @ActionParam(name = "paymentCard")PaymentCard paymentCard){
        log.debug("Registering customer: " + user.getEmail());
        deliveryAddress.setIsDefault(true);
        HashMap<String, Object> stripeResponse = paymentController.createPaymentCustomer(user.getEmail(), paymentCard);
        user.setStripeCustomerId(stripeResponse.get("customerId").toString());
        user.setStripeDefaultSourceId(stripeResponse.get("paymentToken").toString());

        UserController userController = new UserController();
        String userId = userController.addOrUpdateUser(user);
        ControllerContext.set("userId", userId);
        deliveryAddressController.addOrUpdateDeliveryAddress(deliveryAddress);

        log.debug("Registered customer: " + user.getEmail() + " with id " + userId);
        return userId;
    }


}
