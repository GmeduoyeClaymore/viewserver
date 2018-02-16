package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.ShotgunTableUpdater;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import io.viewserver.command.ControllerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Controller(name = "deliveryAddressController")
public class DeliveryAddressController {
    private static final Logger log = LoggerFactory.getLogger(DeliveryAddressController.class);
    private ShotgunTableUpdater shotgunTableUpdater;

    public DeliveryAddressController(ShotgunTableUpdater shotgunTableUpdater) {
        this.shotgunTableUpdater = shotgunTableUpdater;
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
            deliveryAddress.setDeliveryAddressId(ControllerUtils.generateGuid());
            deliveryAddress.setCreated(now);
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

        shotgunTableUpdater.addOrUpdateRow(TableNames.DELIVERY_ADDRESS_TABLE_NAME, "deliveryAddress", deliveryAddressRecord);

        return deliveryAddress.getDeliveryAddressId();
    }
}

