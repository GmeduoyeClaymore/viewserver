package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "vehicleController")
public class VehicleController {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);
    private static String VEHICLE_TABLE_NAME = "/datasources/vehicle/vehicle";

    @ControllerAction(path = "addOrUpdateVehicle", isSynchronous = true)
    public String addOrUpdateVehicle(@ActionParam(name = "vehicle")Vehicle vehicle){
        log.debug("addOrUpdateUser vehicle");
        KeyedTable vehicleTable = ControllerUtils.getKeyedTable(VEHICLE_TABLE_NAME);
        String newVehicleId = ControllerUtils.generateGuid();

        ITableRowUpdater tableUpdater = row -> {
            if(vehicle.getVehicleId() == null){
                row.setString("vehicleId", newVehicleId);
            }
            row.setString("userId", vehicle.getUserId());
            row.setString("registrationNumber", vehicle.getRegistrationNumber());
            row.setString("colour", vehicle.getColour());
            row.setString("make", vehicle.getMake());
            row.setString("model", vehicle.getModel());
            row.setString("vehicleTypeId", vehicle.getVehicleTypeId());
        };

        if(vehicle.getVehicleId() != null){
            vehicleTable.updateRow(new TableKey(vehicle.getVehicleId()), tableUpdater);
            log.debug("Updated vehicle: " + vehicle.getVehicleId());
            return vehicle.getVehicleId();
        }else{
            vehicleTable.addRow(new TableKey(newVehicleId), tableUpdater);
            log.debug("Updated vehicle: " + newVehicleId);
            return newVehicleId;
        }
    }

}
