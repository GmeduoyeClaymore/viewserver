package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.delivery.ProductKey;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface ProductOrderItem extends DynamicJsonBackedObject {
    ProductKey getProduct();
    int getQuantity();
}
