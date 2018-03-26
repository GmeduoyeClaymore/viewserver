import com.shotgun.viewserver.delivery.DeliveryAddress;
import com.shotgun.viewserver.maps.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gbemiga on 15/12/17.
 */
public class MapsControllerTest {

    private MapsController sut;


    @Before
    public void createSut(){
        sut = new MapsController(new MapsControllerKey("AIzaSyBAW_qDo2aiu-AGQ_Ka0ZQXsDvF7lr9p3M",false));
    }

    @Test
    public void canRequestAutoComplete(){
       System.out.println(sut.makeAutoCompleteRequest(new MapRequest("kinnou", "en")));
    }
    @Test
    public void canGetLocationFromPostcode(){

        String postcode = "E59QR";

        HashMap<String, Object> x = sut.makeAutoCompleteRequest(new MapRequest(postcode, "en"));
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

        x = sut.mapPlaceRequest(new PlaceRequest(placeId, "en"));
        x = (HashMap<String, Object>) x.get("result");
        if(x == null || x.size() == 0){
            throw new RuntimeException(String.format("unable to find result for place for postcode \"%s\"", postcode));
        }
        HashMap<String, Object> geometry = (HashMap<String, Object>) x.get("geometry");
        HashMap<String, Object> location = (HashMap<String, Object>) geometry.get("location");
        System.out.println(location);
    }

    @Test
    public void canGetAddressFromLocation(){
        String postcode = "E59QR";
        List<DeliveryAddress> address = sut.getAddressesFromLatLong(sut.getLocationFromPostcode(postcode));
        System.out.println(address);
    }

    @Test
    public void canRequestPlace(){
        System.out.println(sut.mapPlaceRequest(new PlaceRequest("ChIJrTLr-GyuEmsRBfy61i59si0", "en")));
    }

    @Test
    public void canRequestDirections(){
        ArrayList<LatLng> locations = new ArrayList<>();
        locations.add(new LatLng(51.5526951, -0.1412256));
        locations.add(new LatLng(51.4857236, -0.2123406));

        System.out.println(sut.mapDirectionRequest(new DirectionRequest((LatLng[]) locations.toArray(new LatLng[0]), "driving")));
    }

    @Test
    public void canRequestNearby(){
        System.out.println(sut.requestNearbyPlaces(new NearbyPlaceRequest(2.17403, 41.40338, "distance")));
    }
}
