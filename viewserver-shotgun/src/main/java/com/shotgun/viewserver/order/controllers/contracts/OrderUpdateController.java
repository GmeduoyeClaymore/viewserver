package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.datasource.IRecord;

import java.util.Date;

public interface OrderUpdateController {

    default void updateOrderRecord(BasicOrder order) {
        Date now = new Date();

        IRecord orderRecord = new Record().
                addValue("orderId", order.getOrderId()).
                addValue("status", order.getOrderStatus().name()).
                addValue("lastModified", now).
                addValue("orderDetails", order.serialize());


        getDatabaseUpdater().addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);
    }

    IDatabaseUpdater getDatabaseUpdater();

}



