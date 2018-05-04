package com.shotgun.viewserver.user;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface SavedPaymentCard extends DynamicJsonBackedObject {
    String getCardId();
    String getBrand();
    String getLast4();
    Integer getExpMonth();
    Integer getExpYear();
    Boolean getIsDefault();
}

