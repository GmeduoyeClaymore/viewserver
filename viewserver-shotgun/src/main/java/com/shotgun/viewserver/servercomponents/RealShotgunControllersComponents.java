package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.VehicleDetailsApiKey;
import com.shotgun.viewserver.delivery.VehicleDetailsController;
import com.shotgun.viewserver.images.FileSystemImageController;
import com.shotgun.viewserver.images.IImageController;
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
    private IDatabaseUpdater databaseUpdater;
    private NexmoControllerKey controllerKey;
    private StripeApiKey stripeApiKey;
    private ImageUploadLocation imageUploadLocation;
    private MapsControllerKey mapsControllerKey;
    private VehicleDetailsApiKey vehicleDetailsApiKey;
    private ClientVersionInfo clientVersionInfo;

    public RealShotgunControllersComponents(
            IBasicServerComponents basicServerComponents,
            IDatabaseUpdater databaseUpdater,
            NexmoControllerKey controllerKey,
            StripeApiKey stripeApiKey,
            ImageUploadLocation imageUploadLocation,
            MessagingApiKey messagingApiKey,
            MapsControllerKey mapsControllerKey,
            VehicleDetailsApiKey vehicleDetailsApiKey,
            ClientVersionInfo clientVersionInfo) {
        super(basicServerComponents,databaseUpdater);
        this.databaseUpdater = databaseUpdater;
        this.controllerKey = controllerKey;
        this.stripeApiKey = stripeApiKey;
        this.imageUploadLocation = imageUploadLocation;
        this.mapsControllerKey = mapsControllerKey;
        this.vehicleDetailsApiKey = vehicleDetailsApiKey;
        this.clientVersionInfo = clientVersionInfo;
        this.messagingController = new MessagingController(messagingApiKey, this.databaseUpdater, basicServerComponents.getServerCatalog(), messagingApiKey.isBlockRemoteSending());
    }

    @Override
    protected INexmoController getNexmoController() {
        return new NexmoController(this.controllerKey.getDomain(),9000, this.basicServerComponents.getServerCatalog(),controllerKey,  databaseUpdater, messagingController,clientVersionInfo);
    }

    @Override
    protected IPaymentController getPaymentController() {
        if(stripeApiKey.isMockPaymentController()){
            return new MockPaymentController(databaseUpdater,this.basicServerComponents.getServerCatalog());
        }
        return new PaymentController(stripeApiKey, databaseUpdater, this.basicServerComponents.getServerCatalog());
    }

    @Override
    protected IImageController getImageController() {
        return new FileSystemImageController(databaseUpdater,this.basicServerComponents.getServerCatalog(), 9010, imageUploadLocation.getLocation());
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
