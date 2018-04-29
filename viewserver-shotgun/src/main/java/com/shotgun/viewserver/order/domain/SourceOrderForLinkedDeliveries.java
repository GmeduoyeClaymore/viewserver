package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.user.User;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

public interface SourceOrderForLinkedDeliveries extends BasicOrder{
    DeliveryAddress getOrigin();
    DeliveryAddress getProductAddress();
    default LinkedDeliveryOrder createOutboundDelivery(String customerId){
        LinkedDeliveryOrder result = JSONBackedObjectFactory.create(LinkedDeliveryOrder.class);
        result.set("startLocation", getOrigin());
        result.set("endLocation", getProductAddress());
        result.set("customerId", customerId);
        result.set("orderLeg", LinkedDeliveryOrder.OrderLeg.Outbound);
        String orderId = getOrderId();
        if(orderId == null){
            throw new RuntimeException("Unable to create hire delivery order from an unsaved hire order");
        }
        result.set("sourceOrderId", orderId);
        result.setOrderStatus(OrderStatus.PLACED);
        return result;
    }

    default LinkedDeliveryOrder createInboundDelivery(String customerId){
        LinkedDeliveryOrder result = JSONBackedObjectFactory.create(LinkedDeliveryOrder.class);
        result.set("startLocation", getProductAddress());
        result.set("endLocation", getOrigin());
        result.set("customerId", customerId);
        result.set("orderLeg", LinkedDeliveryOrder.OrderLeg.Inbound);
        String orderId = getOrderId();
        if(orderId == null){
            throw new RuntimeException("Unable to create hire delivery order from an unsaved hire order");
        }
        result.set("hireOrderId", orderId);
        result.setOrderStatus(OrderStatus.PLACED);
        return result;
    }
}
