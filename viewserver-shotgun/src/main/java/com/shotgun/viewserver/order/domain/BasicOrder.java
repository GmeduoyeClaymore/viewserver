package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.order.types.OrderContentType;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import java.util.Date;

public interface BasicOrder  extends DynamicJsonBackedObject {

    enum PaymentType{
        FIXED,
        DAYRATE
    }

    PaymentType getPaymentType();

    Integer getAmount();

    String getCustomerUserId();

    String getPartnerUserId();

    String getDescription();

    OrderStatus getOrderStatus();

    OrderContentType getOrderContentType();

    String getPaymentMethodId();

    String getOrderId();

    OrderProduct getOrderProduct();

    void setOrderStatus(OrderStatus status);

    Date getRequiredDate();

    default void transitionTo(OrderStatus status){
        this.setOrderStatus(this.getOrderStatus().transitionTo(status));
    }

}


