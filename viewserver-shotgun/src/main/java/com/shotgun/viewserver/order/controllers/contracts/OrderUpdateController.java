package com.shotgun.viewserver.order.controllers.contracts;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.setup.datasource.OrderDataSource;
import io.viewserver.adapters.common.IDatabaseUpdater;
import io.viewserver.adapters.common.Record;
import io.viewserver.catalog.ICatalog;
import io.viewserver.datasource.IRecord;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Date;

public interface OrderUpdateController {

    default ListenableFuture updateOrderRecord(BasicOrder order) {


        SettableFuture<String> result = SettableFuture.create();

        updateOrderRecordObservable(order).subscribe(res -> {
            result.set(order.getOrderId());
        }, err -> result.setException(err));

        return result;
    }

    default Observable<Boolean> updateOrderRecordObservable(BasicOrder order) {
        Date now = new Date();

        order.set("amountToPay", order.calculateRemainder());

        IRecord orderRecord = new Record().
                addValue("orderId", order.getOrderId()).
                addValue("status", order.getOrderStatus().name()).
                addValue("assignedPartnerUserId", order.getPartnerUserId()).
                addValue("lastModified", now).
                addValue("orderDetails", order.serialize());


        return getDatabaseUpdater().addOrUpdateRow(TableNames.ORDER_TABLE_NAME, OrderDataSource.getDataSource().getSchema(), orderRecord);
    }


    default <T extends BasicOrder> Observable<T> getOrderForId(String orderId, Class<T> orderClass) {
        KeyedTable orderTable = (KeyedTable) getSystemCatalog().getOperatorByPath(TableNames.ORDER_TABLE_NAME);
        return orderTable.waitForRow(new TableKey(orderId), Schedulers.from(ControllerUtils.BackgroundExecutor)).map(
                currentRow -> {
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
        );

    }

    ICatalog getSystemCatalog();

    IDatabaseUpdater getDatabaseUpdater();

}



