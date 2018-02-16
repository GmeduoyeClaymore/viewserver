package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.ShotgunTableUpdater;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.constants.VanProducts;
import com.shotgun.viewserver.constants.VanVolumes;
import com.shotgun.viewserver.delivery.Dimensions;
import com.shotgun.viewserver.delivery.Vehicle;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Controller(name = "vehicleController")
public class VehicleController {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);
    private ShotgunTableUpdater shotgunTableUpdater;

    public VehicleController(ShotgunTableUpdater shotgunTableUpdater) {
        this.shotgunTableUpdater = shotgunTableUpdater;
    }

    @ControllerAction(path = "addOrUpdateVehicle", isSynchronous = true)
    public String addOrUpdateVehicle(Vehicle vehicle) {
        try {
            log.debug("addOrUpdateUser vehicle");
            String userId = (String) ControllerContext.get("userId");
            if (userId == null) {
                throw new RuntimeException("User id must be set in the controller context before this method is called");
            }

            if (vehicle.getVehicleId() == null) {
                vehicle.setVehicleId(ControllerUtils.generateGuid());
            }

            Record vehicleRecord = new Record()
                    .addValue("userId", userId)
                    .addValue("registrationNumber", vehicle.getRegistrationNumber())
                    .addValue("colour", vehicle.getColour())
                    .addValue("make", vehicle.getMake())
                    .addValue("model", vehicle.getModel())
                    .addValue("dimensions", ControllerUtils.toString(vehicle.getDimensions()))
                    .addValue("selectedProductIds", ControllerUtils.toString(vehicle.getSelectedProductIds()))
                    .addValue("bodyStyle", vehicle.getBodyStyle());

            if (vehicle.getNumAvailableForOffload() != null) {
                vehicleRecord.addValue("numAvailableForOffload", vehicle.getNumAvailableForOffload());
            }

            shotgunTableUpdater.addOrUpdateRow(TableNames.VEHICLE_TABLE_NAME, "vehicle", vehicleRecord);
            return vehicle.getVehicleId();

        } catch (Exception e) {
            log.error("There was a problem updating the vehicle", e);
            throw new RuntimeException(e);
        }
    }

    public static List<String> getValidProductsVehicle(Dimensions dimensions) {
        log.debug(String.format("Getting valid products for vehicle with volume %s m cubed", dimensions.getVolumeMetresCubed()));

        if (dimensions.getVolumeMetresCubed() < VanVolumes.MediumVan) {
            log.debug("This is the volume of small van");
            return Arrays.asList(VanProducts.SmallVan);
        } else if (dimensions.getVolumeMetresCubed() < VanVolumes.LargeVan) {
            log.debug("This is the volume of medium van");
            return Arrays.asList(VanProducts.SmallVan, VanProducts.MediumVan);
        } else if (dimensions.getVolumeMetresCubed() < VanVolumes.Luton) {
            log.debug("This is the volume of large van");
            return Arrays.asList(VanProducts.SmallVan, VanProducts.MediumVan, VanProducts.LargeVan);
        } else {
            log.debug("This is the volume of luton");
            return Arrays.asList(VanProducts.SmallVan, VanProducts.MediumVan, VanProducts.LargeVan, VanProducts.Luton);
        }
    }

}
