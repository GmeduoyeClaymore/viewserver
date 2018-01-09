package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.operators.table.ITableRowUpdater;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

@Controller(name = "deliveryAddressController")
public class DeliveryAddressController {
    private static final Logger log = LoggerFactory.getLogger(DeliveryAddressController.class);
    private static String DELIVERY_ADDRESS_TABLE_NAME = "/datasources/deliveryAddress/deliveryAddress";

    @ControllerAction(path = "addOrUpdateDeliveryAddress", isSynchronous = true)
    public String addOrUpdateDeliveryAddress(@ActionParam(name = "userId")String userId, @ActionParam(name = "deliveryAddress")DeliveryAddress deliveryAddress){
        KeyedTable deliveryAddressTable = ControllerUtils.getKeyedTable(DELIVERY_ADDRESS_TABLE_NAME);
        Date now = new Date();
        String newDeliveryAddressId = ControllerUtils.generateGuid();
        log.debug("Adding or updating delivery address");

        ITableRowUpdater tableUpdater = row -> {
            if(deliveryAddress.getDeliveryAddressId() == null){
                row.setString("deliveryAddressId", newDeliveryAddressId);
                row.setLong("created", now.getTime());
            }
            row.setString("userId", userId);
            row.setLong("lastUsed", now.getTime());
            row.setBool("isDefault", deliveryAddress.getIsDefault());
            row.setString("flatNumber", deliveryAddress.getFlatNumber());
            row.setString("line1", deliveryAddress.getLine1());
            row.setString("city", deliveryAddress.getCity());
            row.setString("postCode", deliveryAddress.getPostCode());
            row.setString("googlePlaceId", deliveryAddress.getGooglePlaceId());
            row.setDouble("latitude", deliveryAddress.getLatitude());
            row.setDouble("longitude", deliveryAddress.getLongitude());
        };

        if(deliveryAddress.getDeliveryAddressId() != null){
            deliveryAddressTable.updateRow(new TableKey(deliveryAddress.getDeliveryAddressId()), tableUpdater);
            log.debug("Updated delivery address: " + deliveryAddress.getDeliveryAddressId());
            return deliveryAddress.getDeliveryAddressId();
        }else{
            deliveryAddressTable.addRow(new TableKey(newDeliveryAddressId), tableUpdater);
            log.debug("Added delivery address: " + newDeliveryAddressId);
            return newDeliveryAddressId;
        }
    }
}

