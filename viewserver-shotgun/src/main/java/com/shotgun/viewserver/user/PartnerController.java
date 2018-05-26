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
import com.shotgun.viewserver.payments.PaymentBankAccount;
import com.shotgun.viewserver.payments.IPaymentController;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.table.ITable;
import io.viewserver.reactor.IReactor;
import io.viewserver.reactor.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observable.ListenableFutureObservable;
import rx.schedulers.Schedulers;

import java.util.Date;


@Controller(name = "partnerController")
public class PartnerController {
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
                                                    @ActionParam(name = "vehicle")Vehicle vehicle){

        ITable userTable = ControllerUtils.getTable(TableNames.USER_TABLE_NAME);
        if(this.loginController.getUserRow(userTable,user.getEmail()) != -1){
            throw new RuntimeException("A user is already registered with the email " + user.getEmail());
        }

        log.debug("Registering driver: " + user.getEmail());
        //We can change this later on or on a per user basis
        user.set("chargePercentage", 10);

        //save image if required
        if(user.getImageData() != null){
            String fileName = BucketNames.driverImages + "/" + ControllerUtils.generateGuid() + ".jpg";
            String imageUrl = IImageController.saveImage(BucketNames.shotgunclientimages.name(), fileName, user.getImageData());
            user.set("imageUrl",imageUrl);
        }

        SettableFuture<String> future = SettableFuture.create();
        ControllerContext context = ControllerContext.Current();
        user.set("created",new Date());
        user.set("vehicle",vehicle);
        return ListenableFutureObservable.to(userController.addOrUpdateUserObservable(user, user.getPassword()).observeOn(ControllerContext.Scheduler(context)).map(
                userId -> {
                    context.set("userId",userId, context.getPeerSession());
                    if(user.getDeliveryAddress() != null) {
                        user.getDeliveryAddress().set("isDefault", true);
                        deliveryAddressController.addOrUpdateDeliveryAddress(user.getDeliveryAddress());
                    }
                    log.debug("Registered driver: " + user.getEmail() + " with id " + userId);
                    loginController.setUserIdObservable(userId, context.getPeerSession()).subscribeOn(Schedulers.from(ControllerUtils.BackgroundExecutor)).subscribe(
                            res -> {
                                log.debug("Logged in driver: " + user.getEmail() + " with id " + userId);
                                future.set(userId);
                            },
                            err -> {
                                log.error("Problem logging in user",err);
                                future.setException(err);
                            }
                    );
                    return userId;
                }
        ));
    }


}
