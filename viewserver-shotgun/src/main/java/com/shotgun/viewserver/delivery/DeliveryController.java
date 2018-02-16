package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.ShotgunTableUpdater;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import java.util.Date;

@Controller(name = "deliveryController")
public class DeliveryController {

    private ShotgunTableUpdater shotgunTableUpdater;

    public DeliveryController(ShotgunTableUpdater shotgunTableUpdater) {
        this.shotgunTableUpdater = shotgunTableUpdater;
    }

    @ControllerAction(path = "addOrUpdateDelivery", isSynchronous = true)
    public String addOrUpdateDelivery(@ActionParam(name = "userId") String userId, @ActionParam(name = "delivery") Delivery delivery) {
        Date now = new Date();

        if (delivery.getDeliveryId() == null) {
            delivery.setDeliveryId(ControllerUtils.generateGuid());
            delivery.setCreated(now);
        }

        Record deliveryRecord = new Record()
                .addValue("deliveryId", delivery.getDeliveryId())
                .addValue("lastModified", now)
                .addValue("distance", delivery.getDistance())
                .addValue("duration", delivery.getDuration())
                .addValue("originDeliveryAddressId", delivery.getOrigin().getDeliveryAddressId())
                .addValue("userIdDelivery", userId);


        if (delivery.getFrom() != null) {
            deliveryRecord.addValue("from", delivery.getFrom());
        }

        if (delivery.getTill() != null) {
            deliveryRecord.addValue("till", delivery.getTill());
        }

        if (delivery.getDestination() != null) {//can happen with rubbish collection
            deliveryRecord.addValue("destinationDeliveryAddressId", delivery.getDestination().getDeliveryAddressId());
        }

        shotgunTableUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);

        return delivery.getDeliveryId();
    }

    @ControllerAction(path = "addDriverRating", isSynchronous = true)
    public String addDriverRating(@ActionParam(name = "deliveryId") String deliveryId, @ActionParam(name = "rating") int rating) {
        Record deliveryRecord = new Record()
                .addValue("deliveryId", deliveryId)
                .addValue("driverRating", rating);

        shotgunTableUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);
        return deliveryId;
    }

    @ControllerAction(path = "addCustomerRating", isSynchronous = true)
    public String addCustomerRating(@ActionParam(name = "deliveryId") String deliveryId, @ActionParam(name = "rating") int rating) {
        Record deliveryRecord = new Record()
                .addValue("deliveryId", deliveryId)
                .addValue("customerRating", rating);

        shotgunTableUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);
        return deliveryId;
    }
}

