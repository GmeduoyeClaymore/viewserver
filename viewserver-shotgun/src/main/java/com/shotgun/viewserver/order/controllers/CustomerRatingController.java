package com.shotgun.viewserver.order.controllers;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.delivery.DeliveryAddressController;
import com.shotgun.viewserver.delivery.DeliveryController;
import com.shotgun.viewserver.setup.datasource.RatingDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.command.ActionParam;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(name = "customerRatingController")
public class CustomerRatingController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRatingController.class);

    DeliveryAddressController deliveryAddressController;
    DeliveryController deliveryController;
    private IDatabaseUpdater iDatabaseUpdater;

    public CustomerRatingController(IDatabaseUpdater iDatabaseUpdater,
                                    DeliveryAddressController deliveryAddressController,
                                    DeliveryController deliveryController) {
        this.iDatabaseUpdater = iDatabaseUpdater;
        this.deliveryAddressController = deliveryAddressController;
        this.deliveryController = deliveryController;
    }


    @ControllerAction(path = "addCustomerRating", isSynchronous = true)
    public void addCustomerRating(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        String orderUserId = ControllerUtils.getColumnValue(orderTable, "userId", currentRow).toString();

        Record ratingRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("userId", orderUserId)
                .addValue("rating", rating);

        iDatabaseUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, RatingDataSource.getDataSource().getSchema(), ratingRecord);
    }

    @ControllerAction(path = "addPartnerRating", isSynchronous = true)
    public void addDriverRating(@ActionParam(name = "orderId") String orderId, @ActionParam(name = "rating") int rating) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);
        KeyedTable deliveryTable = ControllerUtils.getKeyedTable(TableNames.DELIVERY_TABLE_NAME);

        String deliveryId = (String) ControllerUtils.getColumnValue(orderTable, "deliveryId", orderId);
        String driverId = (String) ControllerUtils.getColumnValue(deliveryTable, "driverId", deliveryId);

        Record ratingRecord = new Record()
                .addValue("orderId", orderId)
                .addValue("userId", driverId)
                .addValue("rating", rating);

        iDatabaseUpdater.addOrUpdateRow(TableNames.RATING_TABLE_NAME, RatingDataSource.getDataSource().getSchema(), ratingRecord);
    }

}
