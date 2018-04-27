package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import io.viewserver.controller.ControllerAction;

import java.util.HashMap;
import java.util.List;

public interface IMapsController {
    @ControllerAction(path = "requestNearbyPlaces", isSynchronous = false)
    HashMap<String,Object> requestNearbyPlaces(NearbyPlaceRequest request);

    @ControllerAction(path = "mapPlaceRequest", isSynchronous = false)
    HashMap<String,Object> mapPlaceRequest(PlaceRequest request);

    @ControllerAction(path = "getAddressesFromLatLong", isSynchronous = false)
    List<DeliveryAddress> getAddressesFromLatLong(LatLng request);

    @ControllerAction(path = "mapDirectionRequest", isSynchronous = false)
    HashMap<String,Object> mapDirectionRequest(DirectionRequest request);

    @ControllerAction(path = "getLocationFromPostcode", isSynchronous = false)
    LatLng getLocationFromPostcode(String postcode);

    @ControllerAction(path = "makeAutoCompleteRequest", isSynchronous = false)
    HashMap<String,Object> makeAutoCompleteRequest(MapRequest request);

    @ControllerAction(path = "getDistanceAndDuration", isSynchronous = false)
    DistanceAndDuration getDistanceAndDuration(DirectionRequest driving);
}
