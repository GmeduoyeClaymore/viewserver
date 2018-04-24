package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.delivery.IVehicleDetailsController;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.DeliveryOrderController;
import com.shotgun.viewserver.order.OrderController;
import com.shotgun.viewserver.order.OrderItemController;
import com.shotgun.viewserver.order.PricingStrategyResolver;
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
        IImageController IImageController = getImageController();
        IMessagingController messagingController = getMessagingController(); //new MessagingController(configuration.getMessagingApiKey(), getDatabaseUpdater());
        IMapsController IMapsController = getMapsController();
        INexmoController nexmoController = getNexmoController();
        PaymentController paymentController = getPaymentController(); //configuration.isMock() ? new MockPaymentController() : new PaymentControllerImpl(configuration.getStripeKey());


        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(getDatabaseUpdater());
        DeliveryController deliveryController = new DeliveryController(getDatabaseUpdater());
        OrderItemController orderItemController = new OrderItemController(getDatabaseUpdater(), IImageController);
        VehicleController vehicleController = new VehicleController(getDatabaseUpdater());
        LoginController loginController = new LoginController(getDatabaseUpdater(), basicServerComponents.getServerCatalog());
        UserController userController = new UserController(getDatabaseUpdater(), loginController, IImageController, nexmoController, messagingController, IMapsController, getServerReactor());

        this.registerController(paymentController);
        this.registerController(IMapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new PartnerController(getDatabaseUpdater(), paymentController, messagingController, userController, vehicleController, loginController, IImageController, nexmoController, this.getServerReactor()));
        this.registerController(new CustomerController(getDatabaseUpdater(), paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new OrderController(getDatabaseUpdater(), deliveryAddressController, deliveryController, orderItemController, new PricingStrategyResolver(), messagingController));
        this.registerController(new DeliveryOrderController(getDatabaseUpdater(), messagingController, getMapsController(), deliveryAddressController));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(orderItemController);
        this.registerController(IImageController);
        this.registerController(new PhoneCallController(getDatabaseUpdater()));
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
