package com.shotgun.viewserver;

import com.shotgun.viewserver.login.LoginController;
import com.shotgun.viewserver.maps.MapsController;
import com.shotgun.viewserver.maps.MapsControllerKey;
import com.shotgun.viewserver.payments.PaymentController;
import com.shotgun.viewserver.payments.StripeApiKey;
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
        this.registerController(new PaymentController(new StripeApiKey("pk_test_BUWd5f8iUuxmbTT5MqsdOlmk", "sk_test_a36Vq8WXGWEf0Jb55tUUdXD4")));
        this.registerController(new MapsController(new MapsControllerKey("AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M",false)));
        this.registerController(new LoginController());
    }
}


