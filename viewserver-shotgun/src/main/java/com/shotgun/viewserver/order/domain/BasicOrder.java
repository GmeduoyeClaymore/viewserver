package com.shotgun.viewserver.order.domain;

import com.shotgun.viewserver.constants.OrderStatus;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
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

    Integer getAmountToPay();

    String getCustomerUserId();

    String getPartnerUserId();

    String getTitle();

    String getDescription();

    OrderStatus getOrderStatus();

    OrderContentType getOrderContentType();

    String getPaymentMethodId();

    String getOrderId();

    Boolean isJustForFriends();

    OrderProduct getOrderProduct();

    void setOrderStatus(OrderStatus status);

    Date getRequiredDate();

    DeliveryAddress getOrigin();

    Integer getVersion();


    default Integer calculateRemainder(){
        if(getOrderStatus().equals(OrderStatus.COMPLETED)){
            return 0;
        }
        return getAmount();
    }

    default void transitionTo(OrderStatus status){
        this.setOrderStatus(this.getOrderStatus().transitionTo(status));
    }

}


