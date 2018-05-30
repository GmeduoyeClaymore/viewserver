package com.shotgun.viewserver.maps;

import io.viewserver.util.dynamic.DynamicJsonBackedObject;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

public interface LatLng  extends DynamicJsonBackedObject {

    static LatLng from(double latitude, double longitude) {
        LatLng result = JSONBackedObjectFactory.create(LatLng.class);
        result.set("latitude",latitude);
        result.set("longitude", longitude);
        return result;
    }

    double getLatitude();
    double getLongitude();


    default String toQueryString(String key){
        return String.format("result_type=street_address&latlng=%s,%s&key=%s",getLatitude(),getLongitude(),key);
    }
}
