package com.shotgun.viewserver;

import io.viewserver.command.ControllerJSONCommandHandler;
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
        this.registerController(new PaymentController());
    }
}


