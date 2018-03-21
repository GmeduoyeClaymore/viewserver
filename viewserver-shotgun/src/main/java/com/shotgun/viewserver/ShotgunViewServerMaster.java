package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.order.PricingStrategyResolver;
import com.shotgun.viewserver.payments.PaymentControllerImpl;
import com.shotgun.viewserver.user.*;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.order.OrderController;
import com.shotgun.viewserver.order.OrderItemController;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
import io.viewserver.network.IEndpoint;
import io.viewserver.server.*;

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
        localStorageDataAdapterFactory = new FirebaseInstallingDataAdapterFactory(firebaseKeyPath);
    }

    @Override
    protected void initCommandHandlerRegistry() {
        super.initCommandHandlerRegistry();
        ImageController imageController = new ImageController(configuration.getAwsCredentials());
        MessagingController messagingController = new MessagingController(configuration.getMessagingApiKey(), new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath));
        MapsController mapsController = new MapsController(configuration.getMapsKey());
        NexmoController nexmoController = new NexmoController(9000, this.getServerCatalog(), configuration.getNexmoKey(), new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath));
        PaymentController paymentController = new PaymentControllerImpl(configuration.getStripeKey());
        //TODO need to test live stripe payments - StripeApiKey apiKey = isMock ? new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4") : new StripeApiKey("pk_live_7zCPIyqeDeEnLvwzPeS4vXQv", "sk_live_ZZXR0KcIO0s4CswZC3eQrewL");

        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath));
        DeliveryController deliveryController = new DeliveryController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath));
        OrderItemController orderItemController = new OrderItemController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath), imageController);
        VehicleController vehicleController = new VehicleController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath));
        JourneyEmulatorController journeyEmulatorController = new JourneyEmulatorController(mapsController);
        LoginController loginController = new LoginController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath), getServerCatalog());
        UserController userController = new UserController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath), loginController, imageController, nexmoController, mapsController, getServerReactor());

        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new DriverController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath),paymentController,messagingController, userController, vehicleController, journeyEmulatorController, loginController, imageController, nexmoController, this.getServerReactor(), configuration.isMock()));
        this.registerController(new CustomerController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath),paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new OrderController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath), deliveryAddressController,deliveryController,orderItemController, new PricingStrategyResolver(), messagingController));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(orderItemController);
        this.registerController(imageController);
        this.registerController(new PhoneCallController(new FirebaseDatabaseUpdater(getServerExecutionContext(), firebaseKeyPath)));
        this.registerController(nexmoController);
        this.registerController(new VehicleDetailsController(configuration.getVehicleDetailsKey()));
    }

    protected Iterable<IEndpoint> getMasterEndpoints(){
        return configuration.getMasterEndpoints();
    }
}



