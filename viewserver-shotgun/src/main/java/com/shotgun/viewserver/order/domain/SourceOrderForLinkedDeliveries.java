package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.user.User;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

public interface SourceOrderForLinkedDeliveries extends BasicOrder{
    DeliveryAddress getProductAddress();
    default LinkedDeliveryOrder createOutboundDelivery(User user){
        LinkedDeliveryOrder result = JSONBackedObjectFactory.create(LinkedDeliveryOrder.class);
        result.set("startLocation", user.getDeliveryAddress());
        result.set("endLocation", getProductAddress());
        result.set("customerId", user.getUserId());
        result.set("orderLeg", LinkedDeliveryOrder.OrderLeg.Inbound);
        String orderId = getOrderId();
        if(orderId == null){
            throw new RuntimeException("Unable to create hire delivery order from an unsaved hire order");
        }
        result.set("hireOrderId", orderId);
        result.setOrderStatus(OrderStatus.PLACED);
        return result;
    }

    default LinkedDeliveryOrder createInboundDelivery(User user){
        LinkedDeliveryOrder result = JSONBackedObjectFactory.create(LinkedDeliveryOrder.class);
        result.set("startLocation", getProductAddress());
        result.set("endLocation", user.getDeliveryAddress());
        result.set("customerId", user.getUserId());
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
