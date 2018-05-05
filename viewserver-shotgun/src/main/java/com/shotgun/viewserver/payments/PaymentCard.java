package com.shotgun.viewserver.payments;


import com.shotgun.viewserver.user.SavedPaymentCard;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface PaymentCard extends DynamicJsonBackedObject {
    public String getNumber();
    public String getExpMonth();
    public String getExpYear();
    public String getCvc();
    public String getBrand();
}

