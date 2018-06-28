package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.IVehicleDetailsController;
import com.shotgun.viewserver.error.ClientLoggerController;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.controllers.*;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.user.*;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.reactor.IReactor;
import io.viewserver.server.BasicServer;
import io.viewserver.server.components.ControllerComponents;
import io.viewserver.server.components.IBasicServerComponents;
import rx.Observable;


public abstract class ShotgunControllersComponents extends ControllerComponents{


    protected IDatabaseUpdater databaseUpdater;

    public ShotgunControllersComponents(IBasicServerComponents basicServerComponents, IDatabaseUpdater databaseUpdater) {
        super(basicServerComponents);
        this.databaseUpdater = databaseUpdater;
    }

    @Override
    public Observable start() {
        super.start();
        IImageController iImageController = getImageController();
        IMessagingController messagingController = getMessagingController();
        IMapsController mapsController = getMapsController();
        INexmoController nexmoController = getNexmoController();
        IPaymentController paymentController = getPaymentController();


        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(databaseUpdater, basicServerComponents.getServerCatalog());
        LoginController loginController = new LoginController(databaseUpdater, basicServerComponents.getServerCatalog());
        ClientLoggerController clientLoggerController = new ClientLoggerController();
        UserController userController = new UserController(databaseUpdater,  iImageController,  messagingController, paymentController, mapsController, getServerReactor(), basicServerComponents.getServerCatalog());
        DeliveryOrderController deliveryOrderController = new DeliveryOrderController(databaseUpdater, messagingController, deliveryAddressController, paymentController, mapsController, basicServerComponents.getServerCatalog());

        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(clientLoggerController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new PartnerController( paymentController, userController, loginController, iImageController, deliveryAddressController, this.getServerReactor()));
        this.registerController(new CustomerController(deliveryAddressController,userController, loginController, nexmoController));
        this.registerController(deliveryOrderController);
        this.registerController(new HireOrderController(deliveryOrderController, databaseUpdater,mapsController,paymentController, messagingController, basicServerComponents.getServerCatalog()));
        this.registerController(new PersonellOrderController(databaseUpdater, messagingController, deliveryAddressController, paymentController, iImageController, basicServerComponents.getServerCatalog()));
        this.registerController(new ProductOrderController(deliveryOrderController, databaseUpdater,mapsController,paymentController, messagingController, basicServerComponents.getServerCatalog()));
        this.registerController(new RubbishOrderController(databaseUpdater, messagingController, deliveryAddressController, paymentController, mapsController, basicServerComponents.getServerCatalog()));
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(iImageController);
        this.registerController(new PhoneCallController(databaseUpdater, basicServerComponents.getServerCatalog()));
        this.registerController(nexmoController);
        this.registerController(getVehicleDetailsController());
        return null;
    }

    private IReactor getServerReactor() {
        return basicServerComponents.getExecutionContext().getReactor();
    }


    protected abstract INexmoController getNexmoController();
    protected abstract IPaymentController getPaymentController();
    protected abstract IImageController getImageController();
    protected abstract IMessagingController getMessagingController() ;
    protected abstract IMapsController getMapsController() ;
    protected abstract IVehicleDetailsController getVehicleDetailsController() ;


}
