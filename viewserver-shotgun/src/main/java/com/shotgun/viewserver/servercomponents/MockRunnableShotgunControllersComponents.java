package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.IVehicleDetailsController;
import com.shotgun.viewserver.delivery.MockVehicleDetailsController;
import com.shotgun.viewserver.images.FileSystemImageController;
import com.shotgun.viewserver.images.IImageController;
import com.shotgun.viewserver.images.MockImageController;
import com.shotgun.viewserver.maps.IMapsController;
import com.shotgun.viewserver.maps.MockMapsController;
import com.shotgun.viewserver.messaging.IMessagingController;
import com.shotgun.viewserver.messaging.MockMessagingController;
import com.shotgun.viewserver.payments.IPaymentController;
import com.shotgun.viewserver.payments.MockPaymentController;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
import com.shotgun.viewserver.user.INexmoController;
import com.shotgun.viewserver.user.MockNexmoController;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.server.BasicServer;
import io.viewserver.server.components.IBasicServerComponents;

public class MockRunnableShotgunControllersComponents extends ShotgunControllersComponents {

    private String mockDataPath;
    private ImageUploadLocation imageUploadLocation;

    public MockRunnableShotgunControllersComponents(IBasicServerComponents basicServerComponents, IDatabaseUpdater iDatabaseUpdater, String mockDataPath, ImageUploadLocation imageUploadLocation) {
        super(basicServerComponents,iDatabaseUpdater);
        this.mockDataPath = mockDataPath;
        this.imageUploadLocation = imageUploadLocation;
    }

    @Override
    protected INexmoController getNexmoController() {
        return new MockNexmoController();
    }

    @Override
    protected IPaymentController getPaymentController() {
        return new MockPaymentController(databaseUpdater, basicServerComponents.getServerCatalog());
    }

    @Override
    protected IImageController getImageController() {
        return new FileSystemImageController(databaseUpdater,this.basicServerComponents.getServerCatalog(), 9010, imageUploadLocation.getLocation());
    }

    @Override
    protected IMessagingController getMessagingController() {
        return new MockMessagingController(databaseUpdater, basicServerComponents.getServerCatalog());
    }

    @Override
    protected IMapsController getMapsController() {
        return new MockMapsController(mockDataPath);
    }

    @Override
    protected IVehicleDetailsController getVehicleDetailsController() {
        return new MockVehicleDetailsController();
    }
}


