package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;

import java.util.Date;


@Controller(name = "deliveryController")
public class DeliveryController {

    private static String DELIVERY_TABLE_NAME = "/datasources/delivery/delivery";

    @ControllerAction(path = "addOrUpdateDelivery", isSynchronous = true)
    public String addOrUpdateDelivery(@ActionParam(name = "userId")String userId, @ActionParam(name = "delivery")Delivery delivery){
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(DELIVERY_TABLE_NAME);
        Date now = new Date();
        String newDeliveryId = ControllerUtils.generateGuid();

        ITableRowUpdater tableUpdater = row -> {
            if(delivery.getDeliveryId() == null){
                row.setString("deliveryId", newDeliveryId);
                row.setLong("created", now.getTime());
            }
            row.setString("userIdDelivery", userId);
            row.setLong("eta", delivery.getEta().getTime());
            row.setLong("lastModified", now.getTime());
            row.setInt("noRequiredForOffload", delivery.getNoRequiredForOffload());
            row.setString("vehicleTypeId", delivery.getVehicleTypeId());
            row.setString("originDeliveryAddressId", delivery.getOrigin().getDeliveryAddressId());
            if(delivery.getDestination()!=null){//can happen with rubbish collection
                row.setString("destinationDeliveryAddressId", delivery.getDestination().getDeliveryAddressId());
            }
            row.setString("driverId", delivery.getDriverId());
        };

        if(delivery.getDeliveryId() != null){
            deliveryTable.updateRow(new TableKey(delivery.getDeliveryId()), tableUpdater);
            return delivery.getDeliveryId();
        }else{
            deliveryTable.addRow(new TableKey(newDeliveryId), tableUpdater);
            return newDeliveryId;
        }
    }
}

