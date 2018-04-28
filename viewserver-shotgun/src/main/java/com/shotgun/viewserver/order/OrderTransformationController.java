package com.shotgun.viewserver.order;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.TableNames;
import com.shotgun.viewserver.order.domain.BasicOrder;
import io.viewserver.operators.table.KeyedTable;
import io.viewserver.operators.table.TableKey;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface OrderTransformationController extends OrderUpdateController{

    interface ITranformation<TOrder>{
        TOrder call(TOrder order);
    }

    default <T extends BasicOrder> T transform(String orderId, Predicate<T> tranformation, Class<T> orderClass){
        return transform(orderId,tranformation,c->{}, orderClass);
    }
    default <T extends BasicOrder> T transform(String orderId, Predicate<T> tranformation, Consumer<T> afterTransform, Class<T> orderClass){

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();

        T order = JSONBackedObjectFactory.create(orderDetailsString, orderClass);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        if(tranformation.test(order)){
            updateOrderRecord(order);
        }

        afterTransform.accept(order);
        return order;
    }

    default <T extends BasicOrder> T create(String orderId, Predicate<T> tranformation, Consumer<T> afterTransform, Class<T> orderClass){

        KeyedTable orderTable = ControllerUtils.getKeyedTable(TableNames.ORDER_TABLE_NAME);

        int currentRow = orderTable.getRow(new TableKey(orderId));
        if(currentRow == -1){
            throw new RuntimeException("Unable to find order for key:" + orderId);
        }

        String orderDetailsString = ControllerUtils.getColumnValue(orderTable, "orderDetails", currentRow).toString();

        T order = JSONBackedObjectFactory.create(orderDetailsString, orderClass);

        if(order == null){
            throw new RuntimeException("Unable to deserialize order from " + orderDetailsString);
        }

        if(tranformation.test(order)){
            updateOrderRecord(order);
        }

        afterTransform.accept(order);
        return order;
    }

}
