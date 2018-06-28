package com.shotgun.viewserver.maps;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.delivery.orderTypes.types.DeliveryAddress;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.h2.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

@Controller(name = "mapsController")
public class MockMapsController implements IMapsController{

    private String mockDataPath;

    public MockMapsController(String mockDataPath) {
        this.mockDataPath = mockDataPath;
    }

    @Override
    @ControllerAction(path = "requestNearbyPlaces", isSynchronous = false)
    public HashMap<String, Object> requestNearbyPlaces(NearbyPlaceRequest request) {
        return null;
    }

    @Override
    @ControllerAction(path = "mapPlaceRequest", isSynchronous = false)
    public HashMap<String, Object> mapPlaceRequest(PlaceRequest request) {
        String jsonResponse = getJsonStringFromFile(String.format("%s/%s",mockDataPath,"mapPlaceResponse.json"));
        return ControllerUtils.mapDefault(jsonResponse);
    }

    @Override
    @ControllerAction(path = "getAddressesFromLatLong", isSynchronous = false)
    public List<DeliveryAddress> getAddressesFromLatLong(LatLng request) {
        return null;
    }

    @Override
    @ControllerAction(path = "mapDirectionRequest", isSynchronous = false)
    public HashMap<String, Object> mapDirectionRequest(DirectionRequest request) {
        String jsonResponse = getJsonStringFromFile(String.format("%s/%s",mockDataPath,"mapDirectionResponse.json"));
        return ControllerUtils.mapDefault(jsonResponse);
    }

    @Override
    @ControllerAction(path = "getLocationFromPostcode", isSynchronous = false)
    public LatLng getLocationFromPostcode(String postcode) {
        return null;
    }

    @Override
    @ControllerAction(path = "makeAutoCompleteRequest", isSynchronous = false)
    public HashMap<String, Object> makeAutoCompleteRequest(MapRequest request) {
        String jsonResponse = getJsonStringFromFile(String.format("%s/%s",mockDataPath,"makeAutoCompleteResponse.json"));
        return ControllerUtils.mapDefault(jsonResponse);
    }

    @Override
    public DistanceAndDuration getDistanceAndDuration(DirectionRequest driving) {
        return DistanceAndDuration.from(10,10);
    }

    public static String getJsonStringFromFile(String dataFile) {
        InputStream inputStream = MockMapsController.class.getClassLoader().getResourceAsStream(dataFile);
        if (inputStream == null) {
            throw new RuntimeException("Unable to find resource at at " + dataFile);
        }
        Reader reader = IOUtils.getBufferedReader(inputStream);
        try {
            return IOUtils.readStringAndClose(reader, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
