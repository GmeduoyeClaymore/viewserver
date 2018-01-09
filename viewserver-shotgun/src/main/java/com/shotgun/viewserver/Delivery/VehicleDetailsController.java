package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import io.viewserver.command.Controller;
import io.viewserver.command.ControllerAction;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Gbemiga on 09/01/18.
 */
@Controller(name = "vehicleDetailsController")
public class VehicleDetailsController {

    private static String VEHICLE_DETAILS_QUERY_URL = "https://uk1.ukvehicledata.co.uk/api/datapackage/VehicleData";
    private VehicleDetailsApiKey apiKey;
    private static String[] PERMITTED_CLASSES = new String[]{"LCV"};

    public VehicleDetailsController(VehicleDetailsApiKey apiKey) {
        this.apiKey = apiKey;
    }

    @ControllerAction(path = "getDetails")
    public Vehicle getDetails(String registrationNumber){
        VehicleDetailsQuery query = new VehicleDetailsQuery(registrationNumber);
        String json = getJSON(query);
        if(json == null){
            throw new RuntimeException("Null response returned from query");
        }
        Map<String,Object> reqres = ControllerUtils.mapDefault(json);
        Map<String,Object> res = (Map<String, Object>) get(reqres,"Response");
        if(!get(res,"StatusCode").equals("Success")){
            throw new RuntimeException(String.format("Response status code was \"%s\" reason is \"%s\"",res.get("StatusCode"),res.get("StatusMessage")));
        }
        Map<String,Object> dataItems = (Map<String, Object>) get(res, "DataItems");
        Map<String, Object> vehicleRegistration = (Map<String, Object>) get(dataItems, "VehicleRegistration");
        String vehicleTypeId = (String) get(vehicleRegistration,"VehicleClass");
        if(Arrays.binarySearch(PERMITTED_CLASSES,vehicleTypeId) == -1){
            throw new RuntimeException(String.format("Invalid vehicle class \"%s\" found. In order to use this service vehcile must be a type of van",vehicleTypeId));
        }
        Map<String, Object> technicalDetails = (Map<String, Object>) get(dataItems, "TechnicalDetails");
        Map<String, Object> dimensions = (Map<String, Object>) get(technicalDetails, "Dimensions");


        Double length = (Double) get(dimensions, "LoadLength");
        Double width = (Double) get(dimensions, "Width");
        Double height = (Double) get(dimensions, "Height");
        Double weight = (Double) get(dimensions, "PayloadWeight");

        Dimensions dim = new Dimensions(height,width,length,weight);
        String reg = (String)get(vehicleRegistration,"Vrm");
        String make = (String)get(vehicleRegistration,"Make");
        String model = (String)get(vehicleRegistration,"Model");
        String color = (String)get(vehicleRegistration,"Colour");
        return new Vehicle(dim,make,model,vehicleTypeId,color,reg);

    }

    private String getJSON(VehicleDetailsQuery query) {
        if(apiKey.isMock()){
            if(!query.getRegNo().contains("A")){
                throw new RuntimeException("Limited API reg no should contain the letter A");
            }
            URL resource = getClass().getClassLoader().getResource("mock//vehicleDetails.json");
            if(resource == null){
                throw new RuntimeException("Unable to find mock vehicle details");
            }
            return ControllerUtils.urlToString(resource);
        }
        return ControllerUtils.execute("GET", VEHICLE_DETAILS_QUERY_URL, query.toQueryString(apiKey.getKey()));
    }

    private Object get(Map<String, Object> dataItems, String propertyName) {
        Object result = dataItems.get(propertyName);
        if(result == null){
            throw new RuntimeException(String.format("Unable to find property %s in collection containing keys %s",propertyName, String.join(",",dataItems.keySet())));
        }
        return result;
    }


}

