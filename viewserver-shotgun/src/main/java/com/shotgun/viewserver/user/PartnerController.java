package com.shotgun.viewserver.user;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.BucketNames;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.table.ITable;
import io.viewserver.reactor.IReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.observable.ListenableFutureObservable;
import rx.schedulers.Schedulers;

import java.util.Date;
import java.util.concurrent.ExecutionException;


@Controller(name = "partnerController")
public class PartnerController {
    //We can change this later on or on a per user basis
    private final int CHARGE_PERCENTAGE = 5;
    private static final Logger log = LoggerFactory.getLogger(PartnerController.class);
    private IPaymentController paymentController;
    private UserController userController;
    private LoginController loginController;
    private IImageController IImageController;
    private DeliveryAddressController deliveryAddressController;
    private IReactor reactor;

    public PartnerController(IPaymentController paymentController,
                             UserController userController,
                             LoginController loginController,
                             IImageController IImageController,
                             DeliveryAddressController deliveryAddressController,
                             IReactor reactor) {
        this.paymentController = paymentController;
        this.userController = userController;
        this.loginController = loginController;
        this.IImageController = IImageController;
        this.deliveryAddressController = deliveryAddressController;
        this.reactor = reactor;
    }

    @ControllerAction(path = "registerPartner", isSynchronous = false)
    public ListenableFuture<String> registerPartner(@ActionParam(name = "user")User user,
                                                    @ActionParam(name = "vehicle")Vehicle vehicle) throws ExecutionException, InterruptedException {

        ITable userTable = ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        if(this.loginController.getUserRow(userTable,user.getEmail()) != -1){
            throw new RuntimeException("A user is already registered with the email " + user.getEmail());
        }

        log.debug("Registering driver: " + user.getEmail());
        user.set("chargePercentage", CHARGE_PERCENTAGE);
        user.set("type","partner");
        ControllerContext context = ControllerContext.Current();
        user.set("created",new Date());
        user.set("vehicle",vehicle);
        DeliveryAddress deliveryAddress = user.getDeliveryAddress();
        if(deliveryAddress != null){
            user.set("latitude", deliveryAddress.getLatitude());
            user.set("longitude", deliveryAddress.getLongitude());
        }
        return ListenableFutureObservable.to(userController.addOrUpdateUserObservable(user, user.getPassword()).observeOn(ControllerContext.Scheduler(context)).flatMap(
                userId -> {
                    context.set("userId",userId, context.getPeerSession());
                    if(deliveryAddress != null) {
                        deliveryAddress.set("isDefault", true);
                        deliveryAddressController.addOrUpdateDeliveryAddress(deliveryAddress);
                    }
                    log.debug("Registered driver: " + user.getEmail() + " with id " + userId);
                    return loginController.setUserIdObservable(userId, context.getPeerSession()).subscribeOn(Schedulers.from(ControllerUtils.BackgroundExecutor)).map(
                            res -> userId
                    );
                }
        ));
    }


}
