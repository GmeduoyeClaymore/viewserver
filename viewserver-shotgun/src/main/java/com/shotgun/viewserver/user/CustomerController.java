package com.shotgun.viewserver.user;

import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.payments.PaymentCard;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


@Controller(name = "customerController")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @ControllerAction(path = "registerCustomer", isSynchronous = true)
    public String registerCustomer(@ActionParam(name = "user")User user, @ActionParam(name = "deliveryAddress")DeliveryAddress deliveryAddress, @ActionParam(name = "paymentCard")PaymentCard paymentCard){
        log.debug("Registering customer: " + user.getEmail());

        DeliveryAddressController deliveryAddressController = new DeliveryAddressController();
        //TODO - api key should not be in here
        PaymentController paymentController = new PaymentController(new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4"));

        HashMap<String, Object> stripeResponse = paymentController.createPaymentCustomer(user.getEmail(), paymentCard);
        user.setStripeCustomerId(stripeResponse.get("customerId").toString());
        user.setStripeDefaultSourceId(stripeResponse.get("paymentToken").toString());

        UserController userController = new UserController();
        String userId = userController.addOrUpdateUser(user);

        deliveryAddressController.addOrUpdateDeliveryAddress(userId, deliveryAddress);

        log.debug("Registered customer: " + user.getEmail() + " with id " + userId);
        return userId;
    }
}
