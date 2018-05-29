package com.shotgun.viewserver.servercomponents;

import com.amazonaws.auth.BasicAWSCredentials;
import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.images.ImageController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.messaging.MessagingApiKey;
import com.shotgun.viewserver.messaging.MessagingController;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.payments.MockPaymentController;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.user.INexmoController;
import com.shotgun.viewserver.user.NexmoController;
import com.shotgun.viewserver.user.NexmoControllerKey;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.server.components.IBasicServerComponents;

public class RealShotgunControllersComponents extends ShotgunControllersComponents {

    private final MessagingController messagingController;
    private NexmoControllerKey controllerKey;
    private StripeApiKey stripeApiKey;
    private BasicAWSCredentials basicAWSCredentials;
    private MessagingApiKey messagingApiKey;
    private MapsControllerKey mapsControllerKey;
    private VehicleDetailsApiKey vehicleDetailsApiKey;

    public RealShotgunControllersComponents(
            IBasicServerComponents basicServerComponents,
            IDatabaseUpdater databaseUpdater,
            NexmoControllerKey controllerKey,
            StripeApiKey stripeApiKey,
            BasicAWSCredentials basicAWSCredentials,
            MessagingApiKey messagingApiKey,
            MapsControllerKey mapsControllerKey,
            VehicleDetailsApiKey vehicleDetailsApiKey) {
        super(basicServerComponents, databaseUpdater);
        this.controllerKey = controllerKey;
        this.stripeApiKey = stripeApiKey;
        this.basicAWSCredentials = basicAWSCredentials;
        this.messagingApiKey = messagingApiKey;
        this.mapsControllerKey = mapsControllerKey;
        this.vehicleDetailsApiKey = vehicleDetailsApiKey;
        this.messagingController = new MessagingController(messagingApiKey, this.databaseUpdater, basicServerComponents.getServerCatalog(), messagingApiKey.isBlockRemoteSending());
    }

    @Override
    protected INexmoController getNexmoController() {
        return new NexmoController(9000, this.basicServerComponents.getServerCatalog(),controllerKey,  getDatabaseUpdater(), messagingController);
    }

    @Override
    protected IPaymentController getPaymentController() {
        return new PaymentController(stripeApiKey, databaseUpdater, this.basicServerComponents.getServerCatalog());
    }

    @Override
    protected IImageController getImageController() {
        return new ImageController(basicAWSCredentials, databaseUpdater,this.basicServerComponents.getServerCatalog());
    }

    @Override
    protected IMessagingController getMessagingController() {
        return this.messagingController;
    }

    @Override
    protected IMapsController getMapsController() {
        return  new MapsController(mapsControllerKey);
    }

    @Override
    protected VehicleDetailsController getVehicleDetailsController() {
        return new VehicleDetailsController(vehicleDetailsApiKey);
    }
}
