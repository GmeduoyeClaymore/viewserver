package com.shotgun.viewserver.delivery.orderTypes.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shotgun.viewserver.maps.LatLng;
import io.viewserver.util.dynamic.DynamicJsonBackedObject;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface DeliveryAddress extends DynamicJsonBackedObject{
    String getUserId();
    String getDeliveryAddressId();
    String getFlatNumber();
    String getLine1();
    String getCity();
    String getPostCode();
    String getGooglePlaceId();
    Double getLatitude();
    Double getLongitude();
    Boolean getIsDefault();
    String getCountry();
    String getLine2();
    Date getCreated();
    default LatLng getLatLong(){
        if(getLatitude() == null || getLongitude() == null){
            throw new RuntimeException("Cannot get lat lng as latitude or/and longitude are null");
        }
        return new LatLng(getLatitude(), getLongitude());
    }

}
