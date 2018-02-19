package com.shotgun.viewserver;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.constants.TableNames;
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
import io.viewserver.adapters.common.RowUpdater;
import io.viewserver.catalog.ICatalog;
import io.viewserver.core.IExecutionContext;
import io.viewserver.datasource.*;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.server.IViewServerMasterConfiguration;
import io.viewserver.server.ViewServerMaster;

/**
 * Created by nick on 11/08/15.
 */
public class ShotgunViewServerMaster extends ViewServerMaster {
    public ShotgunViewServerMaster(String name, IViewServerMasterConfiguration configuration) {
        super(name, configuration);
    }

    @Override
    protected void initCommandHandlerRegistry() {
        super.initCommandHandlerRegistry();

        PaymentController paymentController = new PaymentController(new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4"));
        ImageController imageController = new ImageController(new BasicAWSCredentials("AKIAJ5IKVCUUR6JC7NCQ", "UYB3e20Jr5jmU7Yk57PzAMyezYyLEQZ5o3lOOrDu"));
        MessagingController messagingController = new MessagingController(new MessagingApiKey("AAAA43sqrgA:APA91bH1hL-tEDjcfzUNxkiyQyvMOToWaTzH7N1g4r6W9TkMSLsPX7TQV_JoIkXkWpWvthr7C57AS5nHXTLKH0Xbz9pZCQgvDM5LpWmJXGVj-3sa_mmoD407IS3NZJv8iTSxNtHQyxZA"), new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        MapsController mapsController = new MapsController(new MapsControllerKey("AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M", false));
        DeliveryAddressController deliveryAddressController = new DeliveryAddressController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        DeliveryController deliveryController = new DeliveryController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        OrderItemController orderItemController = new OrderItemController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry), imageController);
        NexmoController nexmoController = new NexmoController(9000, this.getServerCatalog(), "c03cd396", "33151c6772f2bd52", new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        VehicleController vehicleController = new VehicleController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry));
        JourneyEmulatorController journeyEmulatorController = new JourneyEmulatorController(mapsController);
        LoginController loginController = new LoginController();
        UserController userController = new UserController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry), loginController, imageController, nexmoController);


        this.registerController(paymentController);
        this.registerController(mapsController);
        this.registerController(loginController);
        this.registerController(userController);
        this.registerController(new DriverController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry),paymentController,messagingController, userController, vehicleController, journeyEmulatorController, loginController, imageController, nexmoController, this.getServerReactor()));
        this.registerController(new CustomerController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry),paymentController, deliveryAddressController, messagingController, userController, nexmoController));
        this.registerController(new OrderController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry), deliveryAddressController,deliveryController,orderItemController, new PricingStrategyResolver()));
        this.registerController(vehicleController);
        this.registerController(deliveryController);
        this.registerController(messagingController);
        this.registerController(deliveryAddressController);
        this.registerController(orderItemController);
        this.registerController(imageController);
        this.registerController(new PhoneCallController(new ShotgunTableUpdater(getServerExecutionContext(), dimensionMapper, dataSourceRegistry)));
        this.registerController(nexmoController);
        this.registerController(new VehicleDetailsController(new VehicleDetailsApiKey("881fc904-6ddf-4a48-91ad-7248677ffd1c", true)));
    }
}


