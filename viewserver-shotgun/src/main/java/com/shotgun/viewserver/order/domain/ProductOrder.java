package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.ProductKey;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.Date;
import java.util.List;

public interface ProductOrder extends BasicOrder, NegotiatedOrder, DynamicJsonBackedObject {
    ProductOrderItem[] getProductOrderItems();
}
