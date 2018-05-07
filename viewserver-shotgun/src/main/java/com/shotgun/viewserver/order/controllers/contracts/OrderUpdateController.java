package com.shotgun.viewserver.order.controllers.contracts;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.Date;

public interface OrderUpdateController {

    default void updateOrderRecord(BasicOrder order) {
        Date now = new Date();

        order.set("amountToPay", order.calculateRemainder());

        IRecord orderRecord = new Record().
                addValue("orderId", order.getOrderId()).
                addValue("status", order.getOrderStatus().name()).
                addValue("assignedPartnerUserId", order.getPartnerUserId()).
                addValue("lastModified", now).
                addValue("orderDetails", order.serialize());


        getDatabaseUpdater().addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);
    }


    default <T extends BasicOrder> T getOrderForId(String orderId, Class<T> orderClass) {
        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        Object orderDetails = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow);

        if(orderDetails == null){
            throw new RuntimeException("no orderDetails field found in order row - " + orderId);
        }

        String orderDetailsString = orderDetails.toString();

        T order = JSONBackedObjectFactory.create(orderDetailsString, orderClass);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }
        return order;
    }

    IDatabaseUpdater getDatabaseUpdater();

}



