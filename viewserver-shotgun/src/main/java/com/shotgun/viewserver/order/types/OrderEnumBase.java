package com.shotgun.viewserver.order.types;

import com.shotgun.viewserver.constants.OrderStatus;

public interface OrderEnumBase<TEnum> extends TransitionEnumBase<TEnum> {

    public OrderStatus getOrderStatus() ;

}
