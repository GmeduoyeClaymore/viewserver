package com.shotgun.viewserver.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import com.shotgun.viewserver.maps.LatLng;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.Date;
import java.util.HashMap;


public interface User extends DynamicJsonBackedObject{
    String getUserId();
    String getEmail();
    String getImageData();
    default LatLng getLocation(){
        Double latitude = this.getLatitude();
        Double longitude = this.getLongitude();
        return new LatLng(latitude == null ? 0 : latitude, longitude == null ? 0 : longitude);
    }

    Double getLatitude();
    Double getLongitude();
    String getFirstName();
    String getLastName();
    String getFcmToken();
    DeliveryAddress getDeliveryAddress();
    Date getCreated();
    Date getDob();
    HashMap<String,Object> getSelectedContentTypes();
    String getPassword();
    String getContactNo();
    String getType();
    int getRange();
    String getStripeCustomerId();
    String getStripeDefaultSourceId();
    String getStripeAccountId();
    String getImageUrl();
    int getChargePercentage();
}
