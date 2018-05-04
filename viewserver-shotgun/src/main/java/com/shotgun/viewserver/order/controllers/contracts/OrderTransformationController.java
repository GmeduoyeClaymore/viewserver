package com.shotgun.viewserver.order.controllers.contracts;

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

        T order = getOrderForId(orderId, orderClass);

        if(tranformation.test(order)){
            updateOrderRecord(order);
            afterTransform.accept(order);
        }

        return order;
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


}
