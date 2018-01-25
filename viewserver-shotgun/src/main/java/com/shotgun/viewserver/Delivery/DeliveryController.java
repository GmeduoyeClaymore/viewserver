package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;

import java.util.Date;


@Controller(name = "deliveryController")
public class DeliveryController {

    @ControllerAction(path = "addOrUpdateDelivery", isSynchronous = true)
    public String addOrUpdateDelivery(@ActionParam(name = "userId")String userId, @ActionParam(name = "delivery")Delivery delivery){
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);
        Date now = new Date();
        String newDeliveryId = ControllerUtils.generateGuid();

        ITableRowUpdater tableUpdater = row -> {
            if(delivery.getDeliveryId() == null){
                row.setString("deliveryId", newDeliveryId);
                row.setLong("created", now.getTime());
            }
            row.setString("userIdDelivery", userId);
            if(delivery.getFrom() != null){
                row.setLong("from", delivery.getFrom().getTime());
            }
            if(delivery.getTill() != null){
                row.setLong("till", delivery.getTill().getTime());
            }
            row.setLong("lastModified", now.getTime());
            row.setInt("distance", delivery.getDistance());
            row.setInt("duration", delivery.getDuration());
            row.setString("vehicleTypeId", delivery.getVehicleTypeId());
            row.setString("originDeliveryAddressId", delivery.getOrigin().getDeliveryAddressId());
            if(delivery.getDestination()!= null){//can happen with rubbish collection
                row.setString("destinationDeliveryAddressId", delivery.getDestination().getDeliveryAddressId());
            }
            //String driverId = delivery.getDriverId();
            //row.setString("driverId", driverId);
        };

        if(delivery.getDeliveryId() != null){
            deliveryTable.updateRow(new TableKey(delivery.getDeliveryId()), tableUpdater);
            return delivery.getDeliveryId();
        }else{
            deliveryTable.addRow(new TableKey(newDeliveryId), tableUpdater);
            return newDeliveryId;
        }
    }

    @ControllerAction(path = "addDriverRating", isSynchronous = true)
    public String addDriverRating(@ActionParam(name = "deliveryId")String deliveryId, @ActionParam(name = "rating")int rating){
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        ITableRowUpdater tableUpdater = row -> {
            row.setInt("driverRating", rating);
        };

        deliveryTable.updateRow(new TableKey(deliveryId), tableUpdater);
        return deliveryId;
    }

    @ControllerAction(path = "addCustomerRating", isSynchronous = true)
    public String addCustomerRating(@ActionParam(name = "deliveryId")String deliveryId, @ActionParam(name = "rating")int rating){
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        ITableRowUpdater tableUpdater = row -> {
            row.setInt("customerRating", rating);
        };

        deliveryTable.updateRow(new TableKey(deliveryId), tableUpdater);
        return deliveryId;
    }
}

