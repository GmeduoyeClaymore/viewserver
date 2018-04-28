package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface PersonellOrder extends BasicOrder, VariablePeopleOrder, NegotiatedOrder, DynamicJsonBackedObject, StagedPaymentOrder {
    DeliveryAddress getJobLocation();
}
