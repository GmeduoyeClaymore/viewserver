package com.shotgun.viewserver.order.domain;

public interface SinglePaymentOrder extends BasicOrder{
    public int getAmount();
    public String getPaymentId();
}
