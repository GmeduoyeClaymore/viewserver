package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.delivery.DeliveryAddress;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;

import java.util.HashMap;
import java.util.List;

@Controller(name = "mapsController")
public class MockMapsController implements IMapsController{

    @Override
    @ControllerAction(path = "requestNearbyPlaces", isSynchronous = false)
    public HashMap<String, Object> requestNearbyPlaces(NearbyPlaceRequest request) {
        return null;
    }

    @Override
    @ControllerAction(path = "mapPlaceRequest", isSynchronous = false)
    public HashMap<String, Object> mapPlaceRequest(PlaceRequest request) {
        return null;
    }

    @Override
    @ControllerAction(path = "getAddressesFromLatLong", isSynchronous = false)
    public List<DeliveryAddress> getAddressesFromLatLong(LatLng request) {
        return null;
    }

    @Override
    @ControllerAction(path = "mapDirectionRequest", isSynchronous = false)
    public HashMap<String, Object> mapDirectionRequest(DirectionRequest request) {
        return null;
    }

    @Override
    @ControllerAction(path = "getLocationFromPostcode", isSynchronous = false)
    public LatLng getLocationFromPostcode(String postcode) {
        return null;
    }

    @Override
    @ControllerAction(path = "makeAutoCompleteRequest", isSynchronous = false)
    public HashMap<String, Object> makeAutoCompleteRequest(MapRequest request) {
        return null;
    }
}
