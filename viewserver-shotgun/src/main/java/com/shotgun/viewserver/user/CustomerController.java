package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.login.LoginController;
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
import rx.Observable;

import java.util.HashMap;

import static com.shotgun.viewserver.ControllerUtils.getUserId;


@Controller(name = "customerController")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);


    private DeliveryAddressController deliveryAddressController;
    private UserController userController;
    private LoginController loginController;
    private INexmoController nexmoController;

    public CustomerController(DeliveryAddressController deliveryAddressController,
                              UserController userController,
                              LoginController loginController,
                              INexmoController nexmoController) {

        this.deliveryAddressController = deliveryAddressController;
        this.userController = userController;
        this.loginController = loginController;
        this.nexmoController = nexmoController;
    }

    @ControllerAction(path = "registerCustomer", isSynchronous = true)
    public ListenableFuture<String> registerCustomer(@ActionParam(name = "user")User user, @ActionParam(name = "deliveryAddress")DeliveryAddress deliveryAddress){
        log.debug("Registering customer: " + user.getEmail());

        SettableFuture<String> future = SettableFuture.create();
        String international_format_number = nexmoController.getInternationalFormatNumber(user.getContactNo());
        if(international_format_number == null){
            throw new RuntimeException("Unable to format user contact number " + user.getContactNo() + " is it valid?");
        }
        user.set("contactNo",international_format_number);

        ControllerContext context = ControllerContext.Current();

        userController.addOrUpdateUserObservable(user, user.getPassword()).subscribe(
                userId -> {
                    context.set("userId", userId);

                    if(deliveryAddress != null && deliveryAddress.getLine1() != null) {
                        deliveryAddress.set("isDefault", true);
                        deliveryAddressController.addOrUpdateDeliveryAddress(deliveryAddress);
                    }

                    log.debug("Registered customer: " + user.getEmail() + " with id " + userId);
                    Observable.from(loginController.setUserId(userId)).subscribe(
                            res -> {
                                log.debug("Logged in driver: " + user.getEmail() + " with id " + userId);
                                future.set(userId);
                            },
                            err -> log.error("Problem logging in user",err)
                    );

                },
                err -> {
                    future.setException(err);
                }
        );

        return future;
    }

}
