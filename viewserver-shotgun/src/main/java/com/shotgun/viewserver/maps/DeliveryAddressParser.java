package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;

import java.util.HashMap;
import java.util.List;

public class DeliveryAddressParser{

    public static DeliveryAddress fromReverseGeoSearchResult(HashMap<String, Object> firstResult) {
        HashMap<String, String> componentsByType = new HashMap<>();
        List addressComponents = (List) firstResult.get("address_components");
        if(addressComponents ==null){
            throw new RuntimeException(String.format("Unable to find address componets for result"));
        }
        for (Object obj: addressComponents) {
            HashMap<String,Object> component = (HashMap<String, Object>) obj;
            List<String> types = (List<String>) component.get("types");
            for(String str: types){
                componentsByType.put(str, (String) component.get("long_name"));
            }
        }
        DeliveryAddress result = new DeliveryAddress();
        result.setGooglePlaceId((String)firstResult.get("place_id"));
        HashMap<String, Object> geometry = (HashMap<String, Object>) firstResult.get("geometry");
        HashMap<String, Object> location = (HashMap<String, Object>) geometry.get("location");
        result.setLatitude((Double) location.get("lat"));
        result.setLongitude((Double) location.get("lng"));
        result.setLine1(componentsByType.get("street_number") + " " + componentsByType.get("route"));
        result.setCity(componentsByType.get("postal_town"));
        result.setPostCode(componentsByType.get("postal_code"));
        return result;
    }
}
