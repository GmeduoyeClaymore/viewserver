package com.shotgun.viewserver.delivery;

import io.viewserver.controller.ControllerAction;

public interface IVehicleDetailsController {
    @ControllerAction(path = "getDetails")
    Vehicle getDetails(String registrationNumber);
}
