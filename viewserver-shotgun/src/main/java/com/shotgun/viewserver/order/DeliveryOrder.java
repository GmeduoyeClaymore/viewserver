package com.shotgun.viewserver.order;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.VariablePeopleOrder;

public interface DeliveryOrder extends BasicOrder, VariablePeopleOrder, NegotiatedOrder, DynamicJsonBackedObject {
    DeliveryAddress getOrigin();

    DeliveryAddress getDestination();



}
