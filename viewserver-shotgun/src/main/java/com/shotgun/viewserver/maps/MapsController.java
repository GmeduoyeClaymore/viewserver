package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Controller(name = "mapsController")
public class MapsController implements IMapsController {

    private MapsControllerKey controllerKey;
    private static final Logger logger = LoggerFactory.getLogger(MapsController.class);

    String NEARBY_URL_DEFAULT_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    String REVERSE_GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    String PLACE_URL = "https://maps.googleapis.com/maps/api/place/details/json";
    String DIRECTION_URL = "https://maps.googleapis.com/maps/api/directions/json";
    String AUTO_COMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json";

    public MapsController(MapsControllerKey controllerKey) {
        this.controllerKey = controllerKey;
    }

    @Override
    @ControllerAction(path = "requestNearbyPlaces", isSynchronous = false)
    public HashMap<String,Object> requestNearbyPlaces(NearbyPlaceRequest request){
        return getResponse(request, ControllerUtils.execute("GET", NEARBY_URL_DEFAULT_URL, request.toQueryString(controllerKey.getKey())),true);
    }

    @Override
    @ControllerAction(path = "mapPlaceRequest", isSynchronous = false)
    public HashMap<String,Object> mapPlaceRequest(PlaceRequest request){
        return getResponse(request, ControllerUtils.execute("GET", PLACE_URL, request.toQueryString(controllerKey.getKey())), true);
    }

    @Override
    @ControllerAction(path = "getAddressesFromLatLong", isSynchronous = false)
    public List<DeliveryAddress> getAddressesFromLatLong(LatLng request){
        HashMap<String, Object> get = getResponse(request, ControllerUtils.execute("GET", REVERSE_GEOCODING_URL, request.toQueryString(controllerKey.getKey())), true);
        List resultsInJSON = (List) get.get("results");
        List<DeliveryAddress> results = new ArrayList<>();
        for(Object result : resultsInJSON ){
            DeliveryAddress deliveryAddress = DeliveryAddressParser.fromReverseGeoSearchResult((HashMap<String, Object>) result);
            if(deliveryAddress!=null) {
                results.add(deliveryAddress);
            }
        }
        return results;
    }

    @Override
    @ControllerAction(path = "mapDirectionRequest", isSynchronous = false)
    public HashMap<String,Object> mapDirectionRequest(DirectionRequest request){
        HashMap<String, Object> get = getResponse(request, ControllerUtils.execute("GET", DIRECTION_URL, request.toQueryString(controllerKey.getKey())),false);
        List<String> routes = (List<String>) get.get("routes");
        if(routes== null|| routes.size() == 0){
            throw new RuntimeException("No routes found");
        }
        return get;
    }

    @ControllerAction(path = "getDistanceAndDuration", isSynchronous = false)
    public DistanceAndDuration getDistanceAndDuration(DirectionRequest request){
        HashMap<String, Object> get = getResponse(request, ControllerUtils.execute("GET", DIRECTION_URL, request.toQueryString(controllerKey.getKey())),false);
        return DistanceAndDuration.from(get);
    }
    @Override
    @ControllerAction(path = "getLocationFromPostcode", isSynchronous = false)
    public LatLng getLocationFromPostcode(String postcode){
        HashMap<String, Object> x = this.makeAutoCompleteRequest(new MapRequest(postcode, "en"));
        List result = (List) x.get("predictions");

        if(result == null || result.size() == 0){
            throw new RuntimeException(String.format("unable to find predictions for postcode \"%s\"", postcode));
        }
        HashMap<String, Object> prediction = (HashMap<String, Object>) result.get(0);
        if(prediction == null || prediction.size() == 0){
            throw new RuntimeException(String.format("unable to find prediction for postcode \"%s\"", postcode));
        }

        String placeId = (String) prediction.get("place_id");
        System.out.println();

        x = this.mapPlaceRequest(new PlaceRequest(placeId, "en"));
        x = (HashMap<String, Object>) x.get("result");
        if(x == null || x.size() == 0){
            throw new RuntimeException(String.format("unable to find result for place for postcode \"%s\"", postcode));
        }
        HashMap<String, Object> geometry = (HashMap<String, Object>) x.get("geometry");
        HashMap<String, Object> location = (HashMap<String, Object>) geometry.get("location");
        return new LatLng((Double)location.get("lat"),(Double)location.get("lng"));
    }

    @Override
    @ControllerAction(path = "makeAutoCompleteRequest", isSynchronous = false)
    public HashMap<String,Object> makeAutoCompleteRequest(MapRequest request){
        return getResponse(request, ControllerUtils.execute("GET", AUTO_COMPLETE_URL, request.toQueryString(controllerKey.getKey())),true);
    }

    private HashMap<String, Object> getResponse(Object request, String response, boolean allowZeroResults) {
        HashMap<String,Object> map = ControllerUtils.mapDefault(response);
        List<String> permittedStatus = new ArrayList<>();
        permittedStatus.add("OK");
        if(allowZeroResults){
            permittedStatus.add("ZERO_RESULTS");
        }
        if(!permittedStatus.contains(map.get("status"))){
            if(map.containsKey("error_message")){
                throw new RuntimeException(map.get("error_message") + "");
            }else{
                throw new RuntimeException(String.format("Problem with request \"%s\". Status is \"%s\" response is \"%s\"",request,map.get("status"),response));
            }
        }
        return map;
    }

}

