package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.order.OrderController;
import com.shotgun.viewserver.order.OrderItemController;
import com.shotgun.viewserver.order.PricingStrategyResolver;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.user.*;
import io.viewserver.catalog.Catalog;
import io.viewserver.catalog.CatalogHolder;
import io.viewserver.reactor.IReactor;
import io.viewserver.server.components.ControllerComponents;
import io.viewserver.server.components.IBasicServerComponents;


public abstract class ShotgunControllersComponent extends ControllerComponents{
    private IDatabaseUpdater databaseUpdater;

    public ShotgunControllersComponent(IBasicServerComponents basicServerComponents, IDatabaseUpdater databaseUpdater) {
        super(basicServerComponents);
        this.databaseUpdater = databaseUpdater;
    }

    @Override
    public void start() {
        super.start();
        ImageController imageController = getImageController();
        MessagingController messagingController = getMessagingController(); //new MessagingController(configuration.getMessagingApiKey(), getDatabaseUpdater());
        MapsController mapsController = getMapsController();
        INexmoController nexmoController = getNexmoController();
        PaymentController paymentController = getPaymentController(); //configuration.isMock() ? new MockPaymentController() : new PaymentControllerImpl(configuration.getStripeKey());


        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(getDatabaseUpdater());
        DeliveryController deliveryController = new DeliveryController(getDatabaseUpdater());
        OrderItemController orderItemController = new OrderItemController(getDatabaseUpdater(), imageController);
        VehicleController vehicleController = new VehicleController(getDatabaseUpdater());
        LoginController loginController = new LoginController(getDatabaseUpdater(), basicServerComponents.getServerCatalog());
        UserController userController = new UserController(getDatabaseUpdater(), loginController, imageController, nexmoController, messagingController, mapsController, getServerReactor());

        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new DriverController(getDatabaseUpdater(), paymentController, messagingController, userController, vehicleController, loginController, imageController, nexmoController, this.getServerReactor()));
        this.registerController(new CustomerController(getDatabaseUpdater(), paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new OrderController(getDatabaseUpdater(), deliveryAddressController, deliveryController, orderItemController, new PricingStrategyResolver(), messagingController));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(orderItemController);
        this.registerController(imageController);
        this.registerController(new PhoneCallController(getDatabaseUpdater()));
        this.registerController(nexmoController);
        this.registerController(getVehicleDetailsController());
    }

    private IReactor getServerReactor() {
        return basicServerComponents.getExecutionContext().getReactor();
    }


    protected abstract INexmoController getNexmoController();
    protected abstract PaymentController getPaymentController();
    protected abstract ImageController getImageController();
    protected abstract  MessagingController getMessagingController() ;
    protected abstract  MapsController getMapsController() ;
    protected abstract  VehicleDetailsController getVehicleDetailsController() ;

    public IDatabaseUpdater getDatabaseUpdater() {
        return databaseUpdater;
    }

}
