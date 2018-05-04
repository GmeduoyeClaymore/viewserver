package com.shotgun.viewserver.user;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

public interface UserRating extends DynamicJsonBackedObject {
    enum RatingType{
        Customer,
        Partner
    }
    RatingType getRatingType();
    String getOrderId();
    String getComments();
    String getFromUserId();
    int getRating();
}

