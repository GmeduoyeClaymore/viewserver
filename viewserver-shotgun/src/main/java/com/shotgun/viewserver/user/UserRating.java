package com.shotgun.viewserver.user;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.Date;

public interface UserRating extends DynamicJsonBackedObject {
    enum RatingType{
        Customer,
        Partner
    }
    RatingType getRatingType();
    Date getUpdatedDate();
    String getTitle();
    String getOrderId();
    String[] getImages();
    String getComments();
    String getFromUserId();
    Integer getRating();
}

