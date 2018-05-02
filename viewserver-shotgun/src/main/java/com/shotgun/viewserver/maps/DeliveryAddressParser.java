package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import io.viewserver.util.dynamic.JSONBackedObjectFactory;

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
        DeliveryAddress result = JSONBackedObjectFactory.create(DeliveryAddress.class);
        result.set("googlePlaceId", firstResult.get("place_id"));
        HashMap<String, Object> geometry = (HashMap<String, Object>) firstResult.get("geometry");
        HashMap<String, Object> location = (HashMap<String, Object>) geometry.get("location");
        result.set("latitude",location.get("lat"));
        result.set("longitude",location.get("lng"));
        result.set("line1",componentsByType.get("street_number") + " " + componentsByType.get("route"));
        result.set("city",componentsByType.get("postal_town"));
        result.set("postCode",componentsByType.get("postal_code"));
        return result;
    }
}
