package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface BasicOrder  extends DynamicJsonBackedObject {

    String getCustomerUserId();

    String getPartnerUserId();

    String getDescription();

    OrderStatus getOrderStatus();

    String getPaymentMethodId();

    String getOrderId();

    void setOrderStatus(OrderStatus status);


    default void transitionTo(OrderStatus status){
        this.setOrderStatus(this.getOrderStatus().transitionTo(status));
    }

}


