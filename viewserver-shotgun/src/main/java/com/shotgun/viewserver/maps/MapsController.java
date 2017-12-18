package com.shotgun.viewserver.maps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller(name = "mapsController")
public class MapsController {

    private static TypeReference<HashMap<String,Object>> dictionaryType = new TypeReference<HashMap<String,Object>>() {};
    private static ObjectMapper mapper = new ObjectMapper();
    private MapsControllerKey controllerKey;
    private static final Logger logger = LoggerFactory.getLogger(MapsController.class);

    String NEARBY_URL_DEFAULT_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    String NEARBY_URL_REVERSE_GEOCODING = "https://maps.googleapis.com/maps/api/geocode/json";

    String PLACE_URL = "https://maps.googleapis.com/maps/api/place/details/json";
    String DIRECTION_URL = "https://maps.googleapis.com/maps/api/directions/json";
    String AUTO_COMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json";

    public MapsController(MapsControllerKey controllerKey) {
        this.controllerKey = controllerKey;
    }

    @ControllerAction(path = "requestNearbyPlaces", isSynchronous = false)
    public HashMap<String,Object> requestNearbyPlaces(NearbyPlaceRequest request){
        String url = this.controllerKey.isSupportsReverveGeocoding() ? NEARBY_URL_REVERSE_GEOCODING : NEARBY_URL_DEFAULT_URL;
        return getResponse(request, execute("GET", url, request.toQueryString(controllerKey.getKey(), controllerKey.isSupportsReverveGeocoding())),true);
    }

    @ControllerAction(path = "mapPlaceRequest", isSynchronous = false)
    public HashMap<String,Object> mapPlaceRequest(PlaceRequest request){
        return getResponse(request, execute("GET", PLACE_URL, request.toQueryString(controllerKey.getKey())),true);
    }

    @ControllerAction(path = "mapDirectionRequest", isSynchronous = false)
    public HashMap<String,Object> mapDirectionRequest(DirectionRequest request){
        HashMap<String, Object> get = getResponse(request, execute("GET",DIRECTION_URL , request.toQueryString(controllerKey.getKey())),false);
        List<String> routes = (List<String>) get.get("routes");
        if(routes== null|| routes.size() == 0){
            throw new RuntimeException("No routes found");
        }
        return get;
    }

    @ControllerAction(path = "makeAutoCompleteRequest", isSynchronous = false)
    public HashMap<String,Object> makeAutoCompleteRequest(MapRequest request){
        return getResponse(request, execute("GET",AUTO_COMPLETE_URL , request.toQueryString(controllerKey.getKey())),true);
    }

    private HashMap<String, Object> getResponse(Object request, String response, boolean allowZeroResults) {
        HashMap<String,Object> map = null;
        try {
            map = mapper.readValue(response,dictionaryType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public static String execute(String method, String targetURL, String urlParameters) {
        HttpURLConnection connection = null;
        URL url = null;
        try {
            //Create connection

            url = new URL(targetURL + (method.equals("GET") ? "?" + urlParameters : ""));
            logger.info("Making request to: {}",url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setConnectTimeout(2000);

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            InputStream is = connection.getInputStream();
            InputStream errorStream = connection.getErrorStream();
            if(connection.getResponseCode() != 200){
                throw new RuntimeException("Problem executing request " + getString(errorStream));
            }
            String string = getString(is);
            logger.info("Response to request \"{}\" is \"{}\"",url,string);
            return string;
        } catch (Exception e) {
            logger.error(String.format("Problem making request to: %s",url),e);
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String getString(InputStream is) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
        }
        rd.close();
        return response.toString();
    }
}

