package com.shotgun.viewserver.servercomponents;

import com.shotgun.viewserver.delivery.IVehicleDetailsController;
import com.shotgun.viewserver.delivery.MockVehicleDetailsController;
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
import io.viewserver.server.components.IBasicServerComponents;

public class MockShotgunControllersComponents extends ShotgunControllersComponents {

    public MockShotgunControllersComponents(IBasicServerComponents basicServerComponents, IDatabaseUpdater databaseUpdater) {
        super(basicServerComponents, databaseUpdater);
    }

    @Override
    protected INexmoController getNexmoController() {
        return new MockNexmoController();
    }

    @Override
    protected IPaymentController getPaymentController() {
        return new MockPaymentController(databaseUpdater);
    }

    @Override
    protected IImageController getImageController() {
        return new MockImageController();
    }

    @Override
    protected IMessagingController getMessagingController() {
        return new MockMessagingController(databaseUpdater);
    }

    @Override
    protected IMapsController getMapsController() {
        return new MockMapsController();
    }

    @Override
    protected IVehicleDetailsController getVehicleDetailsController() {
        return new MockVehicleDetailsController();
    }
}


