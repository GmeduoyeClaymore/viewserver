package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.DeliveryAddressDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.controller.ControllerContext;
import io.viewserver.operators.IOutput;
import io.viewserver.operators.IRowSequence;
import io.viewserver.operators.table.ITable;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Controller(name = "deliveryAddressController")
public class DeliveryAddressController {
    private static final Logger log = LoggerFactory.getLogger(DeliveryAddressController.class);
    private IDatabaseUpdater iDatabaseUpdater;

    public DeliveryAddressController(IDatabaseUpdater iDatabaseUpdater) {
        this.iDatabaseUpdater = iDatabaseUpdater;
    }

    @ControllerAction(path = "addOrUpdateDeliveryAddress", isSynchronous = true)
    public String addOrUpdateDeliveryAddress(@ActionParam(name = "deliveryAddress") DeliveryAddress deliveryAddress) {
        Date now = new Date();
        log.debug("Adding or updating delivery address");
        String userId = (String) ControllerContext.get("userId");

        if (userId == null) {
            throw new RuntimeException("User id must be set in the controller context before this method is called");
        }

        if (deliveryAddress.getDeliveryAddressId() == null) {
            String deliveryAddressId = getDeliveryAddressIdFromGooglePlaceId(userId, deliveryAddress.getGooglePlaceId());

            if(deliveryAddressId != null){
                deliveryAddress.set("deliveryAddressId",deliveryAddressId);
            }else{
                deliveryAddress.set("deliveryAddressId",ControllerUtils.generateGuid());
                deliveryAddress.set("created",now);
            }
        }

        Record deliveryAddressRecord = new Record()
                .addValue("deliveryAddressId", deliveryAddress.getDeliveryAddressId())
                .addValue("created", deliveryAddress.getCreated())
                .addValue("userId", userId)
                .addValue("lastUsed", now)
                .addValue("isDefault", deliveryAddress.getIsDefault())
                .addValue("flatNumber", deliveryAddress.getFlatNumber())
                .addValue("line1", deliveryAddress.getLine1())
                .addValue("city", deliveryAddress.getCity())
                .addValue("postCode", deliveryAddress.getPostCode())
                .addValue("googlePlaceId", deliveryAddress.getGooglePlaceId())
                .addValue("latitude", deliveryAddress.getLatitude())
                .addValue("longitude", deliveryAddress.getLongitude());

        iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_ADDRESS_TABLE_NAME, DeliveryAddressDataSource.getDataSource().getSchema(), deliveryAddressRecord);

        return deliveryAddress.getDeliveryAddressId();
    }

    public String getDeliveryAddressIdFromGooglePlaceId(String userId, String googlePlaceId){
        if(googlePlaceId == null || googlePlaceId.trim().equals("")){
            return null;
        }

        KeyedTable deliveryAddressTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_ADDRESS_TABLE_NAME);
        int rowId = deliveryAddressTable.getRow(new TableKey(userId, googlePlaceId));
        return rowId != -1 ? ((String) ControllerUtils.getColumnValue(deliveryAddressTable, "deliveryAddressId", rowId)).toLowerCase() : null;
    }
}

