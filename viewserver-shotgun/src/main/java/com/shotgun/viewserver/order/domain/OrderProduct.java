package com.shotgun.viewserver.order.domain;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface OrderProduct extends DynamicJsonBackedObject {
    String getProductId();
    String getName();
    String getDescription();
    String getCategoryId();
    Integer getPrice();
}
