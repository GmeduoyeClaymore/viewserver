package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.order.PricingStrategyResolver;
import com.shotgun.viewserver.user.*;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.order.OrderController;
import com.shotgun.viewserver.order.OrderItemController;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
import io.viewserver.server.IViewServerMasterConfiguration;
import io.viewserver.server.ViewServerMaster;

/**
 * Created by nick on 11/08/15.
 */
public class ShotgunViewServerMaster extends ViewServerMaster {
    private boolean isMock;

    public ShotgunViewServerMaster(String name, IViewServerMasterConfiguration configuration) {
        super(name, configuration);
        this.getServerExecutionContext().getFunctionRegistry().register("containsProduct",ContainsProduct.class);
        isMock = configuration.isMock();
    }

    @Override
    protected void initCommandHandlerRegistry() {
        super.initCommandHandlerRegistry();

        ImageController imageController = new ImageController(new BasicAWSCredentials("AKIAJ5IKVCUUR6JC7NCQ", "UYB3e20Jr5jmU7Yk57PzAMyezYyLEQZ5o3lOOrDu"));
        MessagingController messagingController = new MessagingController(new MessagingApiKey("AAAA43sqrgA:APA91bH1hL-tEDjcfzUNxkiyQyvMOToWaTzH7N1g4r6W9TkMSLsPX7TQV_JoIkXkWpWvthr7C57AS5nHXTLKH0Xbz9pZCQgvDM5LpWmJXGVj-3sa_mmoD407IS3NZJv8iTSxNtHQyxZA"), new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        MapsController mapsController = new MapsController(new MapsControllerKey("AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M", false));
        NexmoController nexmoController = new NexmoController(9000, this.getServerCatalog(), "c03cd396", "33151c6772f2bd52", new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));

        PaymentController paymentController = this.getPaymentController();
        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        DeliveryController deliveryController = new DeliveryController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        OrderItemController orderItemController = new OrderItemController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry), imageController);
        VehicleController vehicleController = new VehicleController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        JourneyEmulatorController journeyEmulatorController = new JourneyEmulatorController(mapsController);
        LoginController loginController = new LoginController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry), getServerCatalog());
        UserController userController = new UserController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry), loginController, imageController, nexmoController, mapsController, getServerReactor());


        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new DriverController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry),paymentController,messagingController, userController, vehicleController, journeyEmulatorController, loginController, imageController, nexmoController, this.getServerReactor(), isMock));
        this.registerController(new CustomerController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry),paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new OrderController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry), deliveryAddressController,deliveryController,orderItemController, new PricingStrategyResolver(),  messagingController));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(orderItemController);
        this.registerController(imageController);
        this.registerController(new PhoneCallController(new TableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry)));
        this.registerController(nexmoController);
        this.registerController(getVehicleDetailsController());
    }

    private PaymentController getPaymentController(){
        //StripeApiKey apiKey = isMock ? new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4") : new StripeApiKey("pk_live_7zCPIyqeDeEnLvwzPeS4vXQv", "sk_live_ZZXR0KcIO0s4CswZC3eQrewL");
        StripeApiKey apiKey = new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4");
        return new PaymentController(apiKey);
    }

    private VehicleDetailsController getVehicleDetailsController(){
        VehicleDetailsApiKey apiKey = isMock ? new VehicleDetailsApiKey("881fc904-6ddf-4a48-91ad-7248677ffd1c", true) : new VehicleDetailsApiKey("87019DD5-A533-4A7C-A8A1-38BF311C1FF6", false);
        return new VehicleDetailsController(apiKey);
    }

}


