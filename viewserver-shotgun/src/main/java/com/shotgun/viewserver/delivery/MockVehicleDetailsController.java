package com.shotgun.viewserver.delivery;

import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

/**
 * Created by Gbemiga on 09/01/18.
 */


@Controller(name = "vehicleDetailsController")
public class MockVehicleDetailsController implements IVehicleDetailsController{

    @ControllerAction(path = "getDetails")
    public Vehicle getDetails(String registrationNumber) {
        return JSONBackedObjectFactory.create(Vehicle.class);
    }
}
