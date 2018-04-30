package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.order.types.OrderContentType;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface ProductOrder extends BasicOrder, NegotiatedOrder, DynamicJsonBackedObject, SourceOrderForLinkedDeliveries {
    DeliveryAddress getOrigin();
    ProductOrderItem[] getProductOrderItems();
    @Override
    default OrderContentType getOrderContentType(){
        return OrderContentType.Delivery;
    }
}
