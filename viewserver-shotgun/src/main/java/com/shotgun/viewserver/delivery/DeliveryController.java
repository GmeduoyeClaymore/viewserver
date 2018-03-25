package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import java.util.Date;

@Controller(name = "deliveryController")
public class DeliveryController {

    private IDatabaseUpdater iDatabaseUpdater;

    public DeliveryController(IDatabaseUpdater iDatabaseUpdater) {
        this.iDatabaseUpdater = iDatabaseUpdater;
    }

    @ControllerAction(path = "addOrUpdateDelivery", isSynchronous = true)
    public String addOrUpdateDelivery(@ActionParam(name = "customerId") String customerId, @ActionParam(name = "delivery") Delivery delivery) {
        Date now = new Date();

        if (delivery.getDeliveryId() == null) {
            delivery.setDeliveryId(ControllerUtils.generateGuid());
            delivery.setCreated(now);
        }

        Record deliveryRecord = new Record()
                .addValue("deliveryId", delivery.getDeliveryId())
                .addValue("lastModified", now)
                .addValue("distance", delivery.getDistance())
                .addValue("isFixedPrice", delivery.getIsFixedPrice())
                .addValue("fixedPriceValue", delivery.getFixedPriceValue())
                .addValue("duration", delivery.getDuration())
                .addValue("originDeliveryAddressId", delivery.getOrigin().getDeliveryAddressId())
                .addValue("customerId", customerId);


        if (delivery.getFrom() != null) {
            deliveryRecord.addValue("from", delivery.getFrom());
        }

        if (delivery.getTill() != null) {
            deliveryRecord.addValue("till", delivery.getTill());
        }

        if (delivery.getDestination() != null) {//can happen with rubbish collection
            deliveryRecord.addValue("destinationDeliveryAddressId", delivery.getDestination().getDeliveryAddressId());
        }

        iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);

        return delivery.getDeliveryId();
    }

    @ControllerAction(path = "addCustomerRating", isSynchronous = true)
    public String addCustomerRating(@ActionParam(name = "deliveryId") String deliveryId, @ActionParam(name = "rating") int rating) {
        Record deliveryRecord = new Record()
                .addValue("deliveryId", deliveryId)
                .addValue("customerRating", rating);

        iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, "delivery", deliveryRecord);
        return deliveryId;
    }
}

