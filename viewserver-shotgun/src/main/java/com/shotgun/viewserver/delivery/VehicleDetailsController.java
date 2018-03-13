package com.shotgun.viewserver.delivery;

import com.shotgun.viewserver.ControllerUtils;
import com.shotgun.viewserver.constants.VehicleBodyStyles;
import com.shotgun.viewserver.user.VehicleController;
import io.viewserver.controller.Controller;
import io.viewserver.controller.ControllerAction;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Gbemiga on 09/01/18.
 */
@Controller(name = "vehicleDetailsController")
public class VehicleDetailsController {

    private static final Logger log = LoggerFactory.getLogger(VehicleDetailsController.class);
    private static String VEHICLE_DETAILS_QUERY_URL = "https://uk1.ukvehicledata.co.uk/api/datapackage/VehicleData";
    private VehicleDetailsApiKey apiKey;
    private static String[] PERMITTED_BODY_STYLES = new String[]{
            VehicleBodyStyles.BOX_VAN, VehicleBodyStyles.PANEL_VAN,
            VehicleBodyStyles.CAR_DERIVED_VAN, VehicleBodyStyles.LIGHT_VAN,
            VehicleBodyStyles.PICK_UP,
            VehicleBodyStyles.LUTON_VAN, VehicleBodyStyles.INSULATED_VAN,
            VehicleBodyStyles.SPECIALLY_FITTED_VAN, VehicleBodyStyles.DROPSIDE_LORRY};

    public VehicleDetailsController(VehicleDetailsApiKey apiKey) {
        this.apiKey = apiKey;
    }

    @ControllerAction(path = "getDetails")
    public Vehicle getDetails(String registrationNumber){
        try {
            VehicleDetailsQuery query = new VehicleDetailsQuery(registrationNumber);
            String json = getJSON(query);
            if (json == null) {
                log.error(String.format("Null response returned from vehicle details query for reg %s", registrationNumber));
                throw new RuntimeException("There was a problem fetching your vehicle details");
            }
            Map<String, Object> reqres = ControllerUtils.mapDefault(json);
            Map<String, Object> res = (Map<String, Object>) get(reqres, "Response");
            if (!get(res, "StatusCode").equals("Success")) {
                log.error(String.format("Response status code was \"%s\" reason is \"%s\"", res.get("StatusCode"), res.get("StatusMessage")));
                throw new RuntimeException("Unable to find a vehicle with that registration number");
            }
            Map<String, Object> dataItems = (Map<String, Object>) get(res, "DataItems");
            Map<String, Object> vehicleRegistration = (Map<String, Object>) get(dataItems, "VehicleRegistration");
            Map<String, Object> SmmtDetails = (Map<String, Object>) get(dataItems, "SmmtDetails");
            String bodyType = (String) get(SmmtDetails, "BodyStyle");
            if (!Arrays.asList(PERMITTED_BODY_STYLES).contains(bodyType)) {
                throw new RuntimeException(String.format("Invalid vehicle of type \"%s\" found. In order to register as a Shotgun driver your vehicle must be a type of van", bodyType));
            }
            Map<String, Object> technicalDetails = (Map<String, Object>) get(dataItems, "TechnicalDetails");
            Map<String, Object> dimensions = (Map<String, Object>) get(technicalDetails, "Dimensions");


            long weight = ((Double) get(dimensions, "PayloadWeight")).longValue();
            double volume = ((Double) get(dimensions, "PayloadVolume")).doubleValue();

            Dimensions dim = new Dimensions(volume, weight);
            String reg = (String) get(vehicleRegistration, "Vrm");
            String make = WordUtils.capitalizeFully((String) get(vehicleRegistration, "Make"));
            String model = WordUtils.capitalizeFully((String) get(vehicleRegistration, "Model"));
            String color = WordUtils.capitalizeFully((String) get(vehicleRegistration, "Colour"));
            return new Vehicle(dim, make, model, bodyType, color, reg, VehicleController.getValidProductsVehicle(dim).toArray(new String[0]));
        }catch(RuntimeException ex){
            throw ex;
        }catch (Exception ex){
            log.error(String.format("There was a problem fetching vehicle details for reg %s", registrationNumber), ex);
            throw new RuntimeException("There was a problem fetching your vehicle details");
        }
    }

    private String getJSON(VehicleDetailsQuery query) {
        if(apiKey.isMock() && !query.getRegNo().toUpperCase().contains("A")){
            throw new RuntimeException("Limited API reg no should contain the letter A");
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

