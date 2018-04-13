package com.shotgun.viewserver;

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
import com.shotgun.viewserver.payments.MockPaymentController;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.PaymentControllerImpl;
import com.shotgun.viewserver.user.*;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.FirebaseInstallingDataAdapterFactory;
import io.viewserver.server.H2LocalStorageDataAdapterFactory;
import io.viewserver.server.ViewServerMasterBase;

/**
 * Created by nick on 11/08/15.
 */
public class ShotgunViewServerMaster extends ViewServerMasterBase {
    private final String firebaseKeyPath;
    private IShotgunViewServerConfiguration configuration;

    public ShotgunViewServerMaster(String name, IShotgunViewServerConfiguration configuration) {
        super(name);
        this.configuration = configuration;
        this.getServerExecutionContext().getFunctionRegistry().register("containsProduct", ContainsProduct.class);
        firebaseKeyPath = configuration.getFirebaseKeyPath();
        localStorageDataAdapterFactory = configuration.isMock() ? new H2LocalStorageDataAdapterFactory(configuration.getMasterDatabasePath()) : new FirebaseInstallingDataAdapterFactory(firebaseKeyPath);
    }

    @Override
    protected void initCommandHandlerRegistry() {
        super.initCommandHandlerRegistry();
        ImageController imageController = new ImageController(configuration.getAwsCredentials());
        MessagingController messagingController = new MessagingController(configuration.getMessagingApiKey(), getDatabaseUpdater());
        MapsController mapsController = new MapsController(configuration.getMapsKey());
        NexmoController nexmoController = new NexmoController(9000, this.getServerCatalog(), configuration.getNexmoKey(), getDatabaseUpdater());
        PaymentController paymentController = configuration.isMock() ? new MockPaymentController() : new PaymentControllerImpl(configuration.getStripeKey());
        //TODO need to test live stripe payments - StripeApiKey apiKey = isOfflineDb ? new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4") : new StripeApiKey("pk_live_7zCPIyqeDeEnLvwzPeS4vXQv", "sk_live_ZZXR0KcIO0s4CswZC3eQrewL");

        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(getDatabaseUpdater());
        DeliveryController deliveryController = new DeliveryController(getDatabaseUpdater());
        OrderItemController orderItemController = new OrderItemController(getDatabaseUpdater(), imageController);
        VehicleController vehicleController = new VehicleController(getDatabaseUpdater());
        JourneyEmulatorController journeyEmulatorController = new JourneyEmulatorController(mapsController);
        LoginController loginController = new LoginController(getDatabaseUpdater(), getServerCatalog());
        UserController userController = new UserController(getDatabaseUpdater(), loginController, imageController, nexmoController, messagingController, mapsController, getServerReactor());

        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new DriverController(getDatabaseUpdater(),paymentController,messagingController, userController, vehicleController, journeyEmulatorController, loginController, imageController, nexmoController, this.getServerReactor(), configuration.isMock()));
        this.registerController(new CustomerController(getDatabaseUpdater(),paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new OrderController(getDatabaseUpdater(), deliveryAddressController,deliveryController,orderItemController, new PricingStrategyResolver(), messagingController, configuration.isTest()));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(orderItemController);
        this.registerController(imageController);
        this.registerController(new PhoneCallController(getDatabaseUpdater()));
        this.registerController(nexmoController);
        this.registerController(new VehicleDetailsController(configuration.getVehicleDetailsKey()));
    }

    private IDatabaseUpdater getDatabaseUpdater() {
        return configuration.isMock() ? new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry): new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath);
    }

    protected Iterable<IEndpoint> getMasterEndpoints(){
        return configuration.getMasterEndpoints();
    }
}



