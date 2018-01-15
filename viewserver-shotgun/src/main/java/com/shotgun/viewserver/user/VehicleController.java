package com.shotgun.viewserver.user;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.Vehicle;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "vehicleController")
public class VehicleController {
    private static final Logger log = LoggerFactory.getLogger(VehicleController.class);

    @ControllerAction(path = "addOrUpdateVehicle", isSynchronous = true)
    public String addOrUpdateVehicle(@ActionParam(name = "vehicle") Vehicle vehicle){
        log.debug("addOrUpdateUser vehicle");
        KeyedTable vehicleTable = ControllerUtils.getKeyedTable(TableNames.VEHICLE_TABLE_NAME);
        String newVehicleId = ControllerUtils.generateGuid();

        ITableRowUpdater tableUpdater = row -> {
            if(vehicle.getVehicleId() == null){
                row.setString("vehicleId", newVehicleId);
            }
            String userId = (String)ControllerContext.get("userId");
            if(userId == null){
                throw new RuntimeException("User id must be set in the controller context before this method is called");
            }
            row.setString("userId", userId);
            row.setString("registrationNumber", vehicle.getRegistrationNumber());
            row.setString("colour", vehicle.getColour());
            row.setString("make", vehicle.getMake());
            row.setString("model", vehicle.getModel());
            row.setString("dimensions", ControllerUtils.toString(vehicle.getDimensions()));
            row.setString("vehicleTypeId", vehicle.getVehicleTypeId());
            row.setInt("numAvailableForOffload", vehicle.getNumAvailableForOffload());
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
