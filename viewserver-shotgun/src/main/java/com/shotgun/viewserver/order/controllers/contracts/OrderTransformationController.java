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



}
