package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.delivery.IVehicleDetailsController;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.controllers.*;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.user.*;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.reactor.IReactor;
import io.viewserver.server.components.ControllerComponents;
import io.viewserver.server.components.IBasicServerComponents;


public abstract class ShotgunControllersComponents extends ControllerComponents{
    protected IDatabaseUpdater databaseUpdater;

    public ShotgunControllersComponents(IBasicServerComponents basicServerComponents, IDatabaseUpdater databaseUpdater) {
        super(basicServerComponents);
        this.databaseUpdater = databaseUpdater;
    }

    @Override
    public void start() {
        super.start();
        IImageController iImageController = getImageController();
        IMessagingController messagingController = getMessagingController();
        IMapsController mapsController = getMapsController();
        INexmoController nexmoController = getNexmoController();
        PaymentController paymentController = getPaymentController();


        IDatabaseUpdater databaseUpdater = getDatabaseUpdater();
        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(databaseUpdater);
        DeliveryController deliveryController = new DeliveryController(databaseUpdater);
        VehicleController vehicleController = new VehicleController(databaseUpdater);
        LoginController loginController = new LoginController(databaseUpdater, basicServerComponents.getServerCatalog());
        UserController userController = new UserController(databaseUpdater, loginController, iImageController, nexmoController, messagingController, mapsController, getServerReactor());
        DeliveryOrderController deliveryOrderController = new DeliveryOrderController(databaseUpdater, messagingController, deliveryAddressController, paymentController, mapsController);

        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new PartnerController(databaseUpdater, paymentController, messagingController, userController, vehicleController, loginController, iImageController, nexmoController, this.getServerReactor()));
        this.registerController(new CustomerController(databaseUpdater, paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new CustomerRatingController(databaseUpdater, deliveryAddressController, deliveryController));
        this.registerController(deliveryOrderController);
        this.registerController(new HireOrderController(deliveryOrderController, databaseUpdater,mapsController,paymentController, messagingController));
        this.registerController(new PersonellOrderController(databaseUpdater, messagingController, deliveryAddressController, paymentController));
        this.registerController(new ProductOrderController(deliveryOrderController, databaseUpdater,mapsController,paymentController, messagingController));
        this.registerController(new RubbishOrderController(databaseUpdater, messagingController, deliveryAddressController, paymentController));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(iImageController);
        this.registerController(new PhoneCallController(databaseUpdater));
        this.registerController(nexmoController);
        this.registerController(getVehicleDetailsController());
    }

    private IReactor getServerReactor() {
        return basicServerComponents.getExecutionContext().getReactor();
    }


    protected abstract INexmoController getNexmoController();
    protected abstract PaymentController getPaymentController();
    protected abstract IImageController getImageController();
    protected abstract IMessagingController getMessagingController() ;
    protected abstract IMapsController getMapsController() ;
    protected abstract IVehicleDetailsController getVehicleDetailsController() ;

    public IDatabaseUpdater getDatabaseUpdater() {
        return databaseUpdater;
    }

}
