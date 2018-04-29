package com.shotgun.viewserver.order.domain;

public interface LinkedDeliveryOrder extends DeliveryOrder{
    String getSourceOrderId();
    OrderLeg getOrderLeg();
    enum OrderLeg{
        Inbound,
        Outbound
    }
}
