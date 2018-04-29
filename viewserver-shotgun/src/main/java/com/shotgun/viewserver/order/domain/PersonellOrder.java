package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.types.OrderContentType;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface PersonellOrder extends BasicOrder, VariablePeopleOrder, NegotiatedOrder, DynamicJsonBackedObject, StagedPaymentOrder {
    DeliveryAddress getJobLocation();

    @Override
    default OrderContentType getOrderContentType(){
        return OrderContentType.Personell;
    }
}
