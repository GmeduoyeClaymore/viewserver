package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.Vehicle;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.domain.NegotiatedOrder;
import com.shotgun.viewserver.order.types.OrderContentType;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import com.shotgun.viewserver.order.domain.BasicOrder;
import com.shotgun.viewserver.order.domain.VariablePeopleOrder;

public interface DeliveryOrder extends BasicOrder, VariablePeopleOrder, NegotiatedOrder, DynamicJsonBackedObject, JourneyOrder {
    Vehicle getVehicle();
    @Override
    default OrderContentType getOrderContentType(){
        return OrderContentType.Delivery;
    }
}



