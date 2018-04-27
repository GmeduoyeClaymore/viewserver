package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.delivery.IVehicleDetailsController;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.order.*;
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
        IMessagingController messagingController = getMessagingController(); //new MessagingController(configuration.getMessagingApiKey(), getDatabaseUpdater());
        IMapsController mapsController = getMapsController();
        INexmoController nexmoController = getNexmoController();
        PaymentController paymentController = getPaymentController(); //configuration.isMock() ? new MockPaymentController() : new PaymentControllerImpl(configuration.getStripeKey());


        IDatabaseUpdater databaseUpdater = getDatabaseUpdater();
        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(databaseUpdater);
        DeliveryController deliveryController = new DeliveryController(databaseUpdater);
        OrderItemController orderItemController = new OrderItemController(databaseUpdater, iImageController);
        VehicleController vehicleController = new VehicleController(databaseUpdater);
        LoginController loginController = new LoginController(databaseUpdater, basicServerComponents.getServerCatalog());
        UserController userController = new UserController(databaseUpdater, loginController, iImageController, nexmoController, messagingController, mapsController, getServerReactor());

        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new PartnerController(databaseUpdater, paymentController, messagingController, userController, vehicleController, loginController, iImageController, nexmoController, this.getServerReactor()));
        this.registerController(new CustomerController(databaseUpdater, paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new OrderController(databaseUpdater, deliveryAddressController, deliveryController, orderItemController, new PricingStrategyResolver(), messagingController));
        this.registerController(new NegotiatedOrderController(databaseUpdater, messagingController));
        this.registerController(new OrderPaymentController(getPaymentController(), messagingController, databaseUpdater));
        this.registerController(new JourneyBasedOrderController( messagingController, databaseUpdater, getMapsController()));
        this.registerController(new DeliveryOrderController(databaseUpdater, messagingController, deliveryAddressController));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(orderItemController);
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
