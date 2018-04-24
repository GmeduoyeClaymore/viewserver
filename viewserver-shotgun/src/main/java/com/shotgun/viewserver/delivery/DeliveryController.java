package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.adapters.common.IDatabaseUpdater;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.setup.datasource.DeliveryDataSource;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import java.util.Date;

@Controller(name = "deliveryController")
public class DeliveryController {

    private IDatabaseUpdater iDatabaseUpdater;

    public DeliveryController(IDatabaseUpdater databaseUpdater) {
    }

    @ControllerAction(path = "addCustomerRating", isSynchronous = true)
    public String addCustomerRating(@ActionParam(name = "deliveryId") String deliveryId, @ActionParam(name = "rating") int rating) {
        Record deliveryRecord = new Record()
                .addValue("deliveryId", deliveryId)
                .addValue("customerRating", rating);

        iDatabaseUpdater.addOrUpdateRow(TableNames.DELIVERY_TABLE_NAME, DeliveryDataSource.getDataSource().getSchema(), deliveryRecord);
        return deliveryId;
    }
}

